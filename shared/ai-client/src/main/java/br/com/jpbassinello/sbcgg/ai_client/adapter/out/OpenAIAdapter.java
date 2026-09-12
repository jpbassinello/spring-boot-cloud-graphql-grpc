package br.com.jpbassinello.sbcgg.ai_client.adapter.out;

import br.com.jpbassinello.sbcgg.ai_client.application.port.out.LLMPort;
import br.com.jpbassinello.sbcgg.ai_client.config.AiClientConfigProperties;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.restclient.autoconfigure.RestClientBuilderConfigurer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
class OpenAIAdapter implements LLMPort {

  private static final String BASE_URL = "https://api.openai.com/v1";

  private static final int MAX_LOGGED_BODY_CHARS = 500;

  /** {@code incomplete_details.reason} for a generation that ran out of output budget. */
  private static final String REASON_MAX_OUTPUT_TOKENS = "max_output_tokens";

  private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
  private static final Duration READ_TIMEOUT = Duration.ofSeconds(120);

  /** Total attempts (1 initial + retries) for transient transport failures. */
  private static final int MAX_ATTEMPTS = 3;
  private static final long RETRY_BACKOFF_MILLIS = 500;

  private final RestClient restClient;
  private final ObjectMapper objectMapper;
  private final AiClientConfigProperties properties;
  private final MeterRegistry meterRegistry;

  OpenAIAdapter(
      RestClientBuilderConfigurer restClientBuilderConfigurer,
      ObjectMapper objectMapper,
      AiClientConfigProperties properties,
      MeterRegistry meterRegistry) {

    this.properties = properties;
    this.meterRegistry = meterRegistry;
    var snakeCaseConverter = new JacksonJsonHttpMessageConverter(
        JsonMapper.builder()
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .build()
    );
    // Force HTTP/1.1: the JDK client's default HTTP/2 has truncated-response failure modes
    // with the OpenAI API (observed as a 200 whose body is a single "{" byte).
    var jdkHttpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .connectTimeout(CONNECT_TIMEOUT)
        .build();
    var requestFactory = new JdkClientHttpRequestFactory(jdkHttpClient);
    requestFactory.setReadTimeout(READ_TIMEOUT);
    this.restClient = restClientBuilderConfigurer.configure(RestClient.builder())
        .baseUrl(BASE_URL)
        .requestFactory(requestFactory)
        .configureMessageConverters(configurer -> configurer.withJsonConverter(snakeCaseConverter))
        .build();
    this.objectMapper = objectMapper.rebuild()
        .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE).build();
  }

  public <T> Response<T> fetchResponses(String input, LLMPort.Model model, Class<T> responseClass) {
    var gptModel = switch (model) {
      case PREMIUM -> Model.GPT_5_6_SOL;
      case STANDARD -> Model.GPT_5_MINI;
      case LIGHTWEIGHT -> Model.GPT_5_NANO;
    };

    var jsonSchema = String.class.equals(responseClass)
        ? null
        : JsonSchemaGenerator.generate(responseClass);

    var operation = operationTag(responseClass);

    TransientResponseException lastTransient = null;
    for (var attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
      try {
        var textResponse = fetchResponses(input, gptModel, jsonSchema, responseClass, operation);

        if (jsonSchema == null) {
          textResponse = sanitizeLlmJson(textResponse);
        }

        if (String.class.equals(responseClass)) {
          @SuppressWarnings("unchecked")
          var result = (T) textResponse;
          return new Response<>(result, null, null);
        }

        return new Response<>(
            objectMapper.readValue(textResponse, responseClass),
            null,
            null
        );
      } catch (HttpClientErrorException.TooManyRequests e) {
        // Unlike other 4xx, 429 is load-shedding, not a bad request — worth retrying. OpenAI
        // sends Retry-After on these; honor it when present instead of guessing.
        lastTransient = new TransientResponseException("OpenAI rate limited (429)", e);
        var retryAfterMillis = retryAfterMillis(e);
        log.warn("OpenAI rate limited (attempt {}/{}), retry-after={}ms: {}",
            attempt, MAX_ATTEMPTS, retryAfterMillis, e.getMessage());
        if (attempt < MAX_ATTEMPTS) {
          backoff(attempt, retryAfterMillis);
        }
      } catch (HttpClientErrorException e) {
        // Every other 4xx is deterministic — retrying won't help.
        return new Response<>(
            null,
            "Rest client error - " + e.getStatusCode().value(),
            e
        );
      } catch (OutputLimitExceededException e) {
        // The generation exhausted its output budget. A retry would generate the same
        // oversized output and truncate again at the same point — it is a guaranteed
        // second (and third) charge for a guaranteed failure, so fail fast.
        log.warn("OpenAI output limit exceeded, not retrying: {}", e.getMessage());
        return new Response<>(null, "OpenAI output limit exceeded", e);
      } catch (TransientResponseException | ResourceAccessException e) {
        // Truncated/empty body or an I/O / read-timeout failure — the body may arrive
        // intact on a retry, so back off and try again.
        lastTransient = e instanceof TransientResponseException t
            ? t
            : new TransientResponseException(e.getMessage(), e);
        log.warn("OpenAI transient failure (attempt {}/{}): {}",
            attempt, MAX_ATTEMPTS, e.getMessage());
        if (attempt < MAX_ATTEMPTS) {
          backoff(attempt, -1);
        }
      } catch (JacksonException e) {
        // The model emitted malformed JSON for its own content — not transient.
        return new Response<>(
            null,
            "JSON Parse error",
            e
        );
      } catch (Exception e) {
        return new Response<>(
            null,
            "Unexpected error",
            e
        );
      }
    }

    return new Response<>(
        null,
        "OpenAI retryable failure after " + MAX_ATTEMPTS + " attempts",
        lastTransient
    );
  }

  /**
   * {@code overrideMillis >= 0} waits exactly that long (the server's own {@code Retry-After});
   * otherwise falls back to the fixed schedule used for transient transport failures.
   */
  private static void backoff(int attempt, long overrideMillis) {
    var millis = overrideMillis >= 0 ? overrideMillis : RETRY_BACKOFF_MILLIS * attempt;
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /** OpenAI's {@code Retry-After} on a 429, in millis — {@code -1} when absent or unparseable. */
  private static long retryAfterMillis(HttpClientErrorException.TooManyRequests e) {
    var retryAfter = e.getResponseHeaders() == null
        ? null
        : e.getResponseHeaders().getFirst(HttpHeaders.RETRY_AFTER);
    if (retryAfter == null) {
      return -1;
    }
    try {
      return Duration.ofSeconds(Long.parseLong(retryAfter.strip())).toMillis();
    } catch (NumberFormatException ignored) {
      // Retry-After can also be an HTTP-date; not worth parsing for a header OpenAI sends as
      // delta-seconds in practice — fall back to the default schedule.
      return -1;
    }
  }

  private String fetchResponses(
      String input,
      Model model,
      @Nullable Map<String, Object> jsonSchema,
      Class<?> responseClass,
      String operation) {

    var requestBody = new LinkedHashMap<String, Object>();
    requestBody.put("model", model.value);
    requestBody.put("input", input);
    requestBody.put("store", false);
    requestBody.put("max_output_tokens", model.maxOutputTokens);

    if (model.reasoningEffort != null) {
      requestBody.put("reasoning", Map.of("effort", model.reasoningEffort));
    }

    if (jsonSchema != null) {
      var schemaName = JsonSchemaGenerator.toSnakeCase(responseClass.getSimpleName());
      requestBody.put("text", Map.of(
          "format", Map.of(
              "type", "json_schema",
              "name", schemaName,
              "strict", true,
              "schema", jsonSchema
          )
      ));
      log.debug("Using structured output for {}", schemaName);
    }

    var entity = restClient
        .post()
        .uri("/responses")
        .body(requestBody)
        .header("Content-Type", "application/json")
        .header("authorization", "Bearer " + properties.getOpenAi().getApiKey())
        .retrieve()
        .toEntity(String.class);

    var rawBody = entity.getBody();
    if (rawBody == null || rawBody.isBlank()) {
      throw new TransientResponseException(
          "OpenAI returned an empty response body" + describeHeaders(entity.getHeaders()), null);
    }

    OpenAIResponse response;
    try {
      response = objectMapper.readValue(rawBody, OpenAIResponse.class);
    } catch (JacksonException e) {
      // The body reached us truncated or as an unexpected envelope; surface what we
      // actually received (plus transport headers) so the failure is diagnosable.
      var snippet = rawBody.length() > MAX_LOGGED_BODY_CHARS
          ? rawBody.substring(0, MAX_LOGGED_BODY_CHARS) + "…(truncated)"
          : rawBody;
      throw new TransientResponseException(
          "Failed to parse OpenAI response body (" + rawBody.length() + " chars)"
              + describeHeaders(entity.getHeaders()) + ": " + snippet, e);
    }

    // Record usage before any incomplete/truncation check: a truncated generation still
    // billed every token it produced, and those are exactly the ones worth seeing.
    recordUsage(response.usage(), model, operation);

    // A token-truncated generation yields invalid JSON for the model's own content. Surface
    // it as a failure (with the reason) instead of letting it parse-fail downstream as a
    // confusing, non-retriable error.
    if (response.isIncomplete()) {
      var message = "OpenAI response incomplete (reason=" + response.incompleteReason()
          + ", max_output_tokens=" + model.maxOutputTokens + ")";
      if (REASON_MAX_OUTPUT_TOKENS.equals(response.incompleteReason())) {
        throw new OutputLimitExceededException(message);
      }
      throw new TransientResponseException(message, null);
    }

    return response.extractText();
  }

  /**
   * Publishes the response's token accounting as {@code llm.tokens}, split by {@code type} so
   * spend is attributable per operation: {@code input} / {@code input_cached} (prompt tokens,
   * cached ones billed at ~10%) and {@code output} / {@code output_reasoning} (generated
   * tokens; reasoning tokens are a subset of output and billed at the same rate).
   */
  private void recordUsage(@Nullable Usage usage, Model model, String operation) {
    if (usage == null) {
      return;
    }
    count("input", usage.inputTokens(), model, operation);
    count("input_cached", usage.cachedTokens(), model, operation);
    count("output", usage.outputTokens(), model, operation);
    count("output_reasoning", usage.reasoningTokens(), model, operation);
  }

  private void count(String type, long tokens, Model model, String operation) {
    if (tokens <= 0) {
      return;
    }
    meterRegistry.counter("llm.tokens",
        "operation", operation,
        "model", model.value,
        "type", type).increment(tokens);
  }

  /**
   * Attributes spend to a call site. The response type is a faithful proxy for the operation
   * (each use case has its own), except for the free-text {@code String.class} calls, which
   * all collapse into {@code text}.
   */
  private static String operationTag(Class<?> responseClass) {
    return String.class.equals(responseClass)
        ? "text"
        : JsonSchemaGenerator.toSnakeCase(responseClass.getSimpleName());
  }

  private static String describeHeaders(HttpHeaders headers) {
    return " [content-type=" + headers.getFirst(HttpHeaders.CONTENT_TYPE)
        + ", content-length=" + headers.getFirst(HttpHeaders.CONTENT_LENGTH)
        + ", content-encoding=" + headers.getFirst(HttpHeaders.CONTENT_ENCODING)
        + ", transfer-encoding=" + headers.getFirst(HttpHeaders.TRANSFER_ENCODING) + "]";
  }

  /** Signals a recoverable transport-level failure (empty / truncated body) worth retrying. */
  private static final class TransientResponseException extends RuntimeException {
    TransientResponseException(String message, @Nullable Throwable cause) {
      super(message, cause);
    }
  }

  /** Signals a generation that hit {@code max_output_tokens}. Deterministic — never retried. */
  private static final class OutputLimitExceededException extends RuntimeException {
    OutputLimitExceededException(String message) {
      super(message);
    }
  }

  static String sanitizeLlmJson(String text) {
    if (text == null) {
      return null;
    }
    // Strip markdown code fences LLMs occasionally wrap JSON in
    text = text.replaceAll("(?s)^\\s*```(?:json)?\\s*\n?", "")
        .replaceAll("(?s)\n?\\s*```\\s*$", "");

    // Fix OpenAI number formatting quirk
    text = text.replaceAll("0\\. [Nn]ine", "0.9")
        .replaceAll("0\\. [Ee]ight", "0.8");

    return text.trim();
  }

  /**
   * The models we call, each with its reasoning budget and output ceiling.
   *
   * <p>Reasoning tokens are billed as output tokens and count against
   * {@code max_output_tokens}. The GPT-5 family defaults to {@code medium} effort, which for a
   * high-volume classification/extraction path is more than the work needs — but jobs-ai
   * production data showed {@code minimal} makes the model skip the light inference these
   * schema-constrained tasks still require (reading scattered, implicit evidence), degrading
   * fill rates badly. {@code low} is the sweet spot for the nano path: cheaper than the
   * default, but it still reasons.
   *
   * <p>The ceiling moves with the effort: {@code low} spends real reasoning tokens against the
   * same budget, and a generation that hits the ceiling is a non-retriable failure, so 16k
   * leaves headroom while staying half the 32k that {@code medium} needed. The mini path
   * serves user-facing writing (post assessments) at moderate volume, so it keeps the model
   * default and the generous ceiling.
   *
   * <p>The sol path exists for the onboarding profile build, which runs once per search group
   * and whose output — the product context, exclusion contexts and every search phrase — grounds
   * every classification and draft that follows. At that volume the price difference is cents per
   * group, so this is the one call that buys the flagship and asks for {@code high} effort rather
   * than the {@code medium} default: it has to hold several constraints at once (ground every
   * claim in fetched pages, obey the platforms' AND-semantics phrase rules, and resist copying
   * the worked examples' domain). Reasoning tokens bill against the ceiling, so it gets 64k —
   * still half of the model's 128k maximum.
   */
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public enum Model {
    GPT_5_NANO("gpt-5-nano", "low", 16_000),
    GPT_5_MINI("gpt-5-mini", null, 32_000),
    GPT_5_6_SOL("gpt-5.6-sol", "high", 64_000);

    private final String value;

    /** {@code null} leaves the field off the request, i.e. the model's own default. */
    @Nullable
    private final String reasoningEffort;

    private final int maxOutputTokens;
  }

  record OpenAIResponse(
      @Nullable String status,
      @Nullable IncompleteDetails incompleteDetails,
      @Nullable Usage usage,
      List<Output> output) {

    /** True when generation was cut short (e.g. hit the output token limit). */
    boolean isIncomplete() {
      return "incomplete".equals(status);
    }

    @Nullable
    String incompleteReason() {
      return incompleteDetails != null ? incompleteDetails.reason() : null;
    }

    public String extractText() {
      if (output == null) {
        return null;
      }
      return output.stream()
          .map(Output::content)
          .filter(Objects::nonNull)
          .flatMap(List::stream)
          .map(Content::text)
          .filter(Objects::nonNull)
          .findFirst()
          .orElse(null);
    }

    public record Output(List<Content> content) {
    }

    public record Content(String text) {
    }

    public record IncompleteDetails(String reason) {
    }
  }

  /** The response's token accounting. Mirrors the Responses API {@code usage} object. */
  record Usage(
      long inputTokens,
      long outputTokens,
      @Nullable InputTokensDetails inputTokensDetails,
      @Nullable OutputTokensDetails outputTokensDetails) {

    long cachedTokens() {
      return inputTokensDetails != null ? inputTokensDetails.cachedTokens() : 0;
    }

    long reasoningTokens() {
      return outputTokensDetails != null ? outputTokensDetails.reasoningTokens() : 0;
    }

    record InputTokensDetails(long cachedTokens) {
    }

    record OutputTokensDetails(long reasoningTokens) {
    }
  }
}
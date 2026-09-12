package br.com.jpbassinello.sbcgg.ai_client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "ai.client")
public class AiClientConfigProperties {

  private OpenAi openAi;

  @Data
  @Validated
  public static class OpenAi {
    private String apiKey;
  }
}

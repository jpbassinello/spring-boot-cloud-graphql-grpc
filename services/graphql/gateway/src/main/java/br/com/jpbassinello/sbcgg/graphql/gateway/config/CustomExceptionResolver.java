package br.com.jpbassinello.sbcgg.graphql.gateway.config;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ParametersAreNonnullByDefault
@RequiredArgsConstructor
@Slf4j
public class CustomExceptionResolver extends DataFetcherExceptionResolverAdapter {

  private final ObjectMapper objectMapper;

  @Override
  protected GraphQLError resolveToSingleError(Throwable e, DataFetchingEnvironment env) {

    if (e instanceof StatusRuntimeException statusRuntimeException) {
      // matches definitions of GrpcServerExceptionAdvice
      var errorType = switch (statusRuntimeException.getStatus().getCode()) {
        case NOT_FOUND -> ErrorType.NOT_FOUND;
        case UNAUTHENTICATED -> ErrorType.UNAUTHORIZED;
        case INVALID_ARGUMENT -> ErrorType.BAD_REQUEST;
        case PERMISSION_DENIED -> ErrorType.FORBIDDEN;
        default -> ErrorType.INTERNAL_ERROR;
      };

      var extensions = new HashMap<String, Object>();
      var metadata = statusRuntimeException.getTrailers();
      if (metadata != null) {
        var violationsMetadata = metadata.get(Metadata.Key.of("violations", Metadata.ASCII_STRING_MARSHALLER));
        if (violationsMetadata != null) {
          try {
            List<String> violations = objectMapper.readValue(violationsMetadata, new TypeReference<>() {});
            extensions.put("violations", violations);
          } catch (Exception ex) {
            log.warn("Unable to parse violations from grpc metadata", ex);
          }
        }

        var typeMetadata = metadata.get(Metadata.Key.of("type", Metadata.ASCII_STRING_MARSHALLER));
        if (typeMetadata != null) {
          extensions.put("type", typeMetadata);
        }

        var idMetadata = metadata.get(Metadata.Key.of("id", Metadata.ASCII_STRING_MARSHALLER));
        if (idMetadata != null) {
          extensions.put("id", typeMetadata);
        }
      }

      return GraphqlErrorBuilder.newError()
          .errorType(errorType)
          .message(e.getMessage())
          .path(env.getExecutionStepInfo().getPath())
          .location(env.getField().getSourceLocation())
          .extensions(extensions)
          .build();
    }

    if (e instanceof ConstraintViolationException constraintViolationException) {
      var violations = constraintViolationException
          .getConstraintViolations()
          .stream()
          .map(ConstraintViolation::getMessage)
          .distinct()
          .toList();

      var extensions = Map.<String, Object>of("violations", violations);

      return GraphqlErrorBuilder.newError()
          .errorType(ErrorType.BAD_REQUEST)
          .message(e.getMessage())
          .path(env.getExecutionStepInfo().getPath())
          .location(env.getField().getSourceLocation())
          .extensions(extensions)
          .build();
    }

    return super.resolveToSingleError(e, env);
  }
}
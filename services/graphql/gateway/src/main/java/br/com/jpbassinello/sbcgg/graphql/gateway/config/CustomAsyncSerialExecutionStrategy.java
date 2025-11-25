package br.com.jpbassinello.sbcgg.graphql.gateway.config;

import graphql.GraphqlErrorBuilder;
import graphql.execution.AsyncSerialExecutionStrategy;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.SimpleDataFetcherExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
class CustomAsyncSerialExecutionStrategy extends AsyncSerialExecutionStrategy {

  public CustomAsyncSerialExecutionStrategy() {
    super(new GraphQLExceptionHandler());
  }

  @Slf4j
  private static final class GraphQLExceptionHandler extends SimpleDataFetcherExceptionHandler {

    @Override
    public CompletableFuture<DataFetcherExceptionHandlerResult> handleException(
        DataFetcherExceptionHandlerParameters handlerParameters
    ) {
      var exception = handlerParameters.getException();

      if (exception instanceof AccessDeniedException accessDeniedException) {
        var env = handlerParameters.getDataFetchingEnvironment();
        // spring security AccessDeniedException
        return CompletableFuture.completedFuture(DataFetcherExceptionHandlerResult.newResult()
            .error(
                GraphqlErrorBuilder.newError()
                    .errorType(ErrorType.FORBIDDEN)
                    .message(accessDeniedException.getMessage())
                    .path(env.getExecutionStepInfo().getPath())
                    .location(env.getField().getSourceLocation())
                    .build()
            )
            .build());
      }

      return super.handleException(handlerParameters);
    }
  }
}
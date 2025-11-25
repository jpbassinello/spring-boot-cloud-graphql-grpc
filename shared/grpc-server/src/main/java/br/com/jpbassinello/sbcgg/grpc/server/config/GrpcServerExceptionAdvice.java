package br.com.jpbassinello.sbcgg.grpc.server.config;

import br.com.jpbassinello.sbcgg.exception.BadRequestException;
import br.com.jpbassinello.sbcgg.exception.InternalServerErrorException;
import br.com.jpbassinello.sbcgg.exception.ResourceNotFoundException;
import br.com.jpbassinello.sbcgg.exception.TimedOutException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import jakarta.annotation.Nullable;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

import java.util.List;

@GrpcAdvice
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
// needs to be public to be accessed by reflection internally
public class GrpcServerExceptionAdvice {

  private final ObjectMapper objectMapper;

  @GrpcExceptionHandler(ConstraintViolationException.class)
  public StatusRuntimeException handleConstraintViolationException(ConstraintViolationException ex) {
    var violations = ex
        .getConstraintViolations()
        .stream()
        .map(ConstraintViolation::getMessage)
        .distinct()
        .toList();

    return handleConstraintViolations(violations, ex);
  }

  private StatusRuntimeException handleConstraintViolations(@Nullable List<String> violations, Exception e) {
    var metadata = new Metadata();
    if (violations != null && !violations.isEmpty()) {
      try {
        metadata.put(Metadata.Key.of("violations", Metadata.ASCII_STRING_MARSHALLER),
            objectMapper.writeValueAsString(violations));
      } catch (JsonProcessingException ex) {
        log.error("Unexpected trying to convert violations to grpc metadata", e);
      }
    }
    return Status.INVALID_ARGUMENT.withDescription("Constraint violation").withCause(e).asRuntimeException(metadata);
  }

  @GrpcExceptionHandler(ResourceNotFoundException.class)
  public StatusRuntimeException handleResourceNotFoundException(ResourceNotFoundException e) {
    log.warn("Resource not found type={} id={}", e.getType(), e.getId());
    var metadata = new Metadata();
    metadata.put(Metadata.Key.of("type", Metadata.ASCII_STRING_MARSHALLER), e.getType());
    metadata.put(Metadata.Key.of("id", Metadata.ASCII_STRING_MARSHALLER), e.getId());
    return Status.NOT_FOUND.withDescription("Resource not found").withCause(e).asRuntimeException(metadata);
  }

  @GrpcExceptionHandler(BadRequestException.class)
  public StatusRuntimeException handleBadRequestException(BadRequestException e) {
    log.warn("Bad request", e);
    return handleConstraintViolations(e.getViolationCodes(), e);
  }

  @GrpcExceptionHandler(TimedOutException.class)
  public StatusRuntimeException handleTimedOutException(TimedOutException e) {
    log.warn("Request timed out", e);
    return Status.DEADLINE_EXCEEDED.withDescription("Request timed out").withCause(e).asRuntimeException();
  }

  @GrpcExceptionHandler(InternalServerErrorException.class)
  public Status handleNotPreviouslyHandledRuntimeException(InternalServerErrorException e) {
    log.error("Internal server error", e);
    return Status.INTERNAL.withDescription("Internal server error").withCause(e);
  }

  @GrpcExceptionHandler
  public Status handleNotPreviouslyHandledRuntimeException(RuntimeException e) {
    log.error("Unexpected error", e);
    return Status.INTERNAL.withDescription("Unexpected error").withCause(e);
  }
}

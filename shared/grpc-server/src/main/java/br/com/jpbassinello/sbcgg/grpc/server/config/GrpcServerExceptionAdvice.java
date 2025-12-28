package br.com.jpbassinello.sbcgg.grpc.server.config;

import br.com.jpbassinello.sbcgg.exception.BadRequestException;
import br.com.jpbassinello.sbcgg.exception.InternalServerErrorException;
import br.com.jpbassinello.sbcgg.exception.ResourceNotFoundException;
import br.com.jpbassinello.sbcgg.exception.TimedOutException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusException;
import jakarta.annotation.Nullable;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.exception.GrpcExceptionHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GrpcServerExceptionAdvice implements GrpcExceptionHandler {

  private final ObjectMapper objectMapper;

  @Override
  public StatusException handleException(Throwable ex) {
    return switch (ex) {
      case ConstraintViolationException cve -> handleConstraintViolationException(cve);
      case ResourceNotFoundException rnf -> handleResourceNotFoundException(rnf);
      case BadRequestException bre -> handleBadRequestException(bre);
      case TimedOutException toe -> handleTimedOutException(toe);
      case InternalServerErrorException ise -> handleInternalServerErrorException(ise);
      case RuntimeException re -> handleRuntimeException(re);
      default -> handleGenericException(ex);
    };
  }

  private StatusException handleConstraintViolationException(ConstraintViolationException ex) {
    var violations = ex
        .getConstraintViolations()
        .stream()
        .map(ConstraintViolation::getMessage)
        .distinct()
        .toList();

    return createConstraintViolationException(violations, ex);
  }

  private StatusException createConstraintViolationException(@Nullable List<String> violations, Exception e) {
    var metadata = new Metadata();
    if (violations != null && !violations.isEmpty()) {
      try {
        metadata.put(Metadata.Key.of("violations", Metadata.ASCII_STRING_MARSHALLER),
            objectMapper.writeValueAsString(violations));
      } catch (JacksonException ex) {
        log.error("Unexpected trying to convert violations to grpc metadata", e);
      }
    }
    return Status.INVALID_ARGUMENT.withDescription("Constraint violation").withCause(e).asException(metadata);
  }

  private StatusException handleResourceNotFoundException(ResourceNotFoundException e) {
    log.warn("Resource not found type={} id={}", e.getType(), e.getId());
    var metadata = new Metadata();
    metadata.put(Metadata.Key.of("type", Metadata.ASCII_STRING_MARSHALLER), e.getType());
    metadata.put(Metadata.Key.of("id", Metadata.ASCII_STRING_MARSHALLER), e.getId());
    return Status.NOT_FOUND.withDescription("Resource not found").withCause(e).asException(metadata);
  }

  private StatusException handleBadRequestException(BadRequestException e) {
    log.warn("Bad request", e);
    return createConstraintViolationException(e.getViolationCodes(), e);
  }

  private StatusException handleTimedOutException(TimedOutException e) {
    log.warn("Request timed out", e);
    return Status.DEADLINE_EXCEEDED.withDescription("Request timed out").withCause(e).asException();
  }

  private StatusException handleInternalServerErrorException(InternalServerErrorException e) {
    log.error("Internal server error", e);
    return Status.INTERNAL.withDescription("Internal server error").withCause(e).asException();
  }

  private StatusException handleRuntimeException(RuntimeException e) {
    log.error("Unexpected error", e);
    return Status.INTERNAL.withDescription("Unexpected error").withCause(e).asException();
  }

  private StatusException handleGenericException(Throwable e) {
    log.error("Unexpected error", e);
    return Status.INTERNAL.withDescription("Unexpected error").withCause(e).asException();
  }
}

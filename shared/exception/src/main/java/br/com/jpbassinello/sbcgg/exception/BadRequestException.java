package br.com.jpbassinello.sbcgg.exception;


import lombok.Getter;

import java.util.List;

public final class BadRequestException extends RuntimeException {

  /**
   * Violation code is used to propagate information from services to clients
   * For example, when trying to register a user that already exists in the system with the provided email address
   * Try to always use codes in the format:
   * domain.field.violation
   * Example: user.email.alreadyRegistered
   */
  @Getter
  private List<String> violationCodes = List.of();

  public BadRequestException(String message, Throwable exception) {
    super(message, exception);
  }

  public BadRequestException(Throwable exception) {
    super(exception);
  }

  public BadRequestException(String message) {
    super(message);
  }

  public BadRequestException withViolationCodes(List<String> violationCodes) {
    this.violationCodes = violationCodes;
    return this;
  }
}

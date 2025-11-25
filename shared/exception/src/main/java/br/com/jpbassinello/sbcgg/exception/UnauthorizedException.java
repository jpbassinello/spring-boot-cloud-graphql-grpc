package br.com.jpbassinello.sbcgg.exception;


public final class UnauthorizedException extends RuntimeException {

  public UnauthorizedException(String message, Throwable exception) {
    super(message, exception);
  }

  public UnauthorizedException(Throwable exception) {
    super(exception);
  }

  public UnauthorizedException(String message) {
    super(message);
  }
}

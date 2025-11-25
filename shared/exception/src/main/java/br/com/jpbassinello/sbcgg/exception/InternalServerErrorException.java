package br.com.jpbassinello.sbcgg.exception;


public final class InternalServerErrorException extends RuntimeException {

  public InternalServerErrorException(String message, Throwable exception) {
    super(message, exception);
  }

  public InternalServerErrorException(Throwable exception) {
    super(exception);
  }

  public InternalServerErrorException(String message) {
    super(message);
  }
}

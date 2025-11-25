package br.com.jpbassinello.sbcgg.exception;


public final class TimedOutException extends RuntimeException {

  public TimedOutException(String message, Throwable exception) {
    super(message, exception);
  }

  public TimedOutException(Throwable exception) {
    super(exception);
  }

  public TimedOutException(String message) {
    super(message);
  }
}

package br.com.jpbassinello.sbcgg.exception;


public final class RateLimitException extends RuntimeException {

  public RateLimitException(String message) {
    super(message);
  }
}

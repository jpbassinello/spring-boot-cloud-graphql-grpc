package br.com.jpbassinello.sbcgg.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {

  private final String type;
  private final String id;

  public ResourceNotFoundException(String message, String type, String id) {
    super(message);
    this.type = type;
    this.id = id;
  }
}

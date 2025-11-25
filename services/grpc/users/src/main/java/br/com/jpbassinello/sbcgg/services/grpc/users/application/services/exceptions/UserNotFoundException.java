package br.com.jpbassinello.sbcgg.services.grpc.users.application.services.exceptions;

import br.com.jpbassinello.sbcgg.exception.ResourceNotFoundException;

public final class UserNotFoundException extends ResourceNotFoundException {

  public UserNotFoundException(String key) {
    super("User not found", "user", key);
  }
}

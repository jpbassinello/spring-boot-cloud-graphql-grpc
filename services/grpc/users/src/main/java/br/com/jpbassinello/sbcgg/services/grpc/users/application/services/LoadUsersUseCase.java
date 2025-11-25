package br.com.jpbassinello.sbcgg.services.grpc.users.application.services;

import br.com.jpbassinello.sbcgg.jpa.domain.entities.SimplePage;
import br.com.jpbassinello.sbcgg.services.grpc.users.application.port.out.LoadUserPort;
import br.com.jpbassinello.sbcgg.services.grpc.users.application.services.exceptions.UserNotFoundException;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.entities.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Validated
@ParametersAreNonnullByDefault
@Slf4j
public class LoadUsersUseCase {
  private final LoadUserPort loadUser;

  public User loadUserById(UUID id) {
    return loadUser.loadUserById(id)
        .orElseThrow(() -> new UserNotFoundException(id.toString()));
  }

  public User loadUserByEmail(String email) {
    return loadUser.loadUserByEmail(email)
        .orElseThrow(() -> new UserNotFoundException(email));
  }

  public SimplePage<User> searchUsers(String terms, int page, int pageSize) {
    return loadUser.search(terms, page, pageSize);
  }
}

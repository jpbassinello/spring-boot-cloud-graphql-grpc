package br.com.jpbassinello.sbcgg.services.grpc.users.application.port.out;

import br.com.jpbassinello.sbcgg.jpa.domain.entities.SimplePage;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.entities.User;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
public interface LoadUserPort {

  Optional<User> loadUserById(UUID id);

  Optional<User> loadUserByEmail(String email);

  Optional<User> loadUserByMobilePhoneNumber(String mobilePhoneNumber);

  SimplePage<User> search(String terms, int page, int pageSize);
}

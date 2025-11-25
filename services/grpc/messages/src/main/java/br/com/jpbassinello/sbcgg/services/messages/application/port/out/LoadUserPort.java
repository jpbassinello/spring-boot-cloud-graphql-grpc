package br.com.jpbassinello.sbcgg.services.messages.application.port.out;

import br.com.jpbassinello.sbcgg.services.messages.domain.entities.User;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
public interface LoadUserPort {

  Optional<User> loadUserById(UUID userId);

}

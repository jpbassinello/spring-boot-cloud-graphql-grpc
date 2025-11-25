package br.com.jpbassinello.sbcgg.graphql.gateway.application.port.out;

import br.com.jpbassinello.sbcgg.graphql.gateway.domain.types.User;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.types.UserPage;
import jakarta.annotation.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
public interface LoadUserPort {

  Optional<User> loadUserById(UUID id);

  Optional<User> loadUserByEmail(String email);

  UserPage search(@Nullable String terms, int page, int pageSize);
}

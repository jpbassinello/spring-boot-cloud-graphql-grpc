package br.com.jpbassinello.sbcgg.services.messages.adapter.out.users;

import br.com.jpbassinello.sbcgg.grpc.interfaces.users.LoadUserRequest;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.UsersServiceGrpc;
import br.com.jpbassinello.sbcgg.services.messages.application.port.out.LoadUserPort;
import br.com.jpbassinello.sbcgg.services.messages.domain.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.UUID;

@Component
@ParametersAreNonnullByDefault
@RequiredArgsConstructor
class UserGrpcAdapter implements LoadUserPort {

  private final UsersServiceGrpc.UsersServiceBlockingStub usersGrpc;

  @Override
  public Optional<User> loadUserById(UUID userId) {
    var response = usersGrpc.loadUser(
        LoadUserRequest.newBuilder().setId(userId.toString()).build()
    );

    return response.hasUser()
        ? Optional.ofNullable(UserGrpcMapper.INSTANCE.mapToDomain(response.getUser()))
        : Optional.empty();
  }

}
package br.com.jpbassinello.sbcgg.services.messages.adapter.out.users;

import br.com.jpbassinello.sbcgg.grpc.interfaces.users.LoadUserRequest;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.UsersServiceGrpc;
import br.com.jpbassinello.sbcgg.services.messages.application.port.out.LoadUserPort;
import br.com.jpbassinello.sbcgg.services.messages.domain.entities.User;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.UUID;

@Component
@ParametersAreNonnullByDefault
class UserGrpcAdapter implements LoadUserPort {

  @GrpcClient("users")
  private UsersServiceGrpc.UsersServiceBlockingStub usersGrpc;

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

package br.com.jpbassinello.sbcgg.services.messages.adapter.out.users;

import br.com.jpbassinello.sbcgg.grpc.interfaces.users.LoadUserRequest;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.UsersServiceGrpc;
import br.com.jpbassinello.sbcgg.services.messages.application.port.out.LoadUserPort;
import br.com.jpbassinello.sbcgg.services.messages.domain.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.stereotype.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.UUID;

@Component
@ParametersAreNonnullByDefault
@RequiredArgsConstructor
class UserGrpcAdapter implements LoadUserPort {

  private final GrpcChannelFactory channels;
  private UsersServiceGrpc.UsersServiceBlockingStub usersGrpc;

  private synchronized UsersServiceGrpc.UsersServiceBlockingStub getUsersGrpc() {
    if (usersGrpc == null) {
      usersGrpc = UsersServiceGrpc.newBlockingStub(channels.createChannel("users"));
    }
    return usersGrpc;
  }

  @Override
  public Optional<User> loadUserById(UUID userId) {
    var response = getUsersGrpc().loadUser(
        LoadUserRequest.newBuilder().setId(userId.toString()).build()
    );

    return response.hasUser()
        ? Optional.ofNullable(UserGrpcMapper.INSTANCE.mapToDomain(response.getUser()))
        : Optional.empty();
  }

}
package br.com.jpbassinello.sbcgg.graphql.gateway.adapter.out.user;

import br.com.jpbassinello.sbcgg.graphql.gateway.application.port.out.LoadUserPort;
import br.com.jpbassinello.sbcgg.graphql.gateway.application.port.out.RegisterUserPort;
import br.com.jpbassinello.sbcgg.graphql.gateway.application.port.out.VerifyUserContactMethodPort;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.enums.Role;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.enums.UserContactMethod;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.inputs.RegisterUserInput;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.types.User;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.types.UserPage;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.LoadUserRequest;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.LoadUserResponse;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.RegisterUserRequest;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.SearchUsersRequest;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.UsersServiceGrpc;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.VerifyContactMethodRequest;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@ParametersAreNonnullByDefault
@RequiredArgsConstructor
class UserGrpcAdapter implements LoadUserPort, RegisterUserPort, VerifyUserContactMethodPort {

  private static final UserGrpcMapper USER_GRPC_MAPPER = UserGrpcMapper.INSTANCE;

  private final UsersServiceGrpc.UsersServiceBlockingStub usersGrpc;

  @Override
  public Optional<User> loadUserById(UUID id) {

    return Optional.of(
            usersGrpc.loadUser(LoadUserRequest.newBuilder().setId(id.toString()).build())
        ).filter(LoadUserResponse::hasUser)
        .map(LoadUserResponse::getUser)
        .map(USER_GRPC_MAPPER::mapToType);
  }

  @Override
  public Optional<User> loadUserByEmail(String email) {
    return Optional.of(
            usersGrpc.loadUser(LoadUserRequest.newBuilder().setEmail(email).build())
        ).filter(LoadUserResponse::hasUser)
        .map(LoadUserResponse::getUser)
        .map(USER_GRPC_MAPPER::mapToType);
  }

  @Override
  public User register(RegisterUserInput input, List<Role> roles) {
    var userInput = USER_GRPC_MAPPER.mapToProto(input)
        .toBuilder()
        .addAllRoles(roles.stream().map(USER_GRPC_MAPPER::mapToProto).toList())
        .build();

    var response = usersGrpc.registerUser(
        RegisterUserRequest.newBuilder()
            .setInput(userInput)
            .build()
    );

    return USER_GRPC_MAPPER.mapToType(response.getUser());
  }

  @Override
  public void verifyUserContactMethod(UUID userId, String code, UserContactMethod userContactMethod) {
    var method = USER_GRPC_MAPPER.mapToProto(userContactMethod);

    usersGrpc.verifyContactMethod(
        VerifyContactMethodRequest.newBuilder()
            .setUserId(userId.toString())
            .setMethod(method)
            .setCode(code)
            .build()
    );
  }

  @Override
  public UserPage search(@Nullable String terms, int page, int pageSize) {
    var response = usersGrpc.searchUsers(
        SearchUsersRequest.newBuilder()
            .setTerms(Optional.ofNullable(terms).orElse(""))
            .setPage(page)
            .setPageSize(pageSize)
            .build()
    );

    return  new UserPage(
        response.getUsersList().stream().map(USER_GRPC_MAPPER::mapToType).toList(),
        response.getHasNext()
    );
  }
}
package br.com.jpbassinello.sbcgg.services.grpc.users.adapter.in;

import br.com.jpbassinello.sbcgg.grpc.interfaces.users.LoadUserRequest;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.LoadUserResponse;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.RegisterUserRequest;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.RegisterUserResponse;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.SearchUsersRequest;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.SearchUsersResponse;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.UsersServiceGrpc;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.VerifyContactMethodRequest;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.VerifyContactMethodResponse;
import br.com.jpbassinello.sbcgg.services.grpc.users.application.services.LoadUsersUseCase;
import br.com.jpbassinello.sbcgg.services.grpc.users.application.services.ManageUsersUseCase;
import br.com.jpbassinello.sbcgg.services.grpc.users.application.services.VerifyUserUseCase;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
class UsersGrpcAdapter extends UsersServiceGrpc.UsersServiceImplBase {

  private static final UserGrpcMapper USER_GRPC_MAPPER = UserGrpcMapper.INSTANCE;

  private final LoadUsersUseCase loadAccounts;
  private final ManageUsersUseCase manageAccounts;
  private final VerifyUserUseCase verifyUsers;

  @Override
  @Transactional(readOnly = true)
  public void loadUser(LoadUserRequest request, StreamObserver<LoadUserResponse> responseObserver) {

    var response = LoadUserResponse.newBuilder();

    if (StringUtils.isNotBlank(request.getId())) {
      response.setUser(
          USER_GRPC_MAPPER.mapToProto(
              loadAccounts.loadUserById(UUID.fromString(request.getId()))
          )
      );
    } else if (StringUtils.isNotBlank(request.getEmail())) {
      response.setUser(
          USER_GRPC_MAPPER.mapToProto(
              loadAccounts.loadUserByEmail(request.getEmail())
          )
      );
    }

    responseObserver.onNext(response.build());
    responseObserver.onCompleted();
  }

  @Override
  @Transactional
  public void registerUser(RegisterUserRequest request, StreamObserver<RegisterUserResponse> responseObserver) {
    var response = RegisterUserResponse.newBuilder()
        .setUser(
            USER_GRPC_MAPPER.mapToProto(
                manageAccounts.register(
                    USER_GRPC_MAPPER.mapToInput(request.getInput())
                )
            )
        ).build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  @Transactional(readOnly = true)
  public void searchUsers(SearchUsersRequest request, StreamObserver<SearchUsersResponse> responseObserver) {
    var pageResult = loadAccounts.searchUsers(request.getTerms(), request.getPage(), request.getPageSize());

    var response = SearchUsersResponse.newBuilder()
        .addAllUsers(pageResult.items().stream().map(USER_GRPC_MAPPER::mapToProto).toList())
        .setHasNext(pageResult.hasNext())
        .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void verifyContactMethod(VerifyContactMethodRequest request, StreamObserver<VerifyContactMethodResponse> responseObserver) {

    var userId = UUID.fromString(request.getUserId());
    var contactType = USER_GRPC_MAPPER.mapToEnum(request.getMethod());
    var code = request.getCode();

    verifyUsers.verify(
        VerifyUserUseCase.VerifyRequest
            .builder()
            .userId(userId)
            .type(contactType)
            .code(code)
            .build()
    );

    responseObserver.onNext(VerifyContactMethodResponse.getDefaultInstance());
    responseObserver.onCompleted();
  }
}

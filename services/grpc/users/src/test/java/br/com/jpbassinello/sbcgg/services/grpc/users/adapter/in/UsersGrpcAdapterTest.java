package br.com.jpbassinello.sbcgg.services.grpc.users.adapter.in;

import br.com.jpbassinello.sbcgg.grpc.interfaces.users.LoadUserRequest;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.LoadUserResponse;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.RegisterUserRequest;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.RegisterUserResponse;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.SearchUsersRequest;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.SearchUsersResponse;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.UserInput;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.UserRole;
import br.com.jpbassinello.sbcgg.jpa.domain.entities.SimplePage;
import br.com.jpbassinello.sbcgg.services.grpc.users.application.services.LoadUsersUseCase;
import br.com.jpbassinello.sbcgg.services.grpc.users.application.services.ManageUsersUseCase;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.entities.User;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.enums.Role;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsersGrpcAdapterTest {

  private static final UUID USER_ID = UUID.randomUUID();
  private static final ZonedDateTime NOW = ZonedDateTime.now();

  private static final User USER = User.builder()
      .id(USER_ID)
      .firstName("João")
      .lastName("Bassinello")
      .email("joao@sbcgg.com")
      .emailVerified(true)
      .mobilePhoneNumber("+5519987654321")
      .mobilePhoneNumberVerified(true)
      .active(true)
      .registeredAt(NOW.minusDays(10))
      .roles(List.of(Role.ADMIN, Role.USER))
      .timeZoneId("America/Sao_Paulo")
      .build();

  private static final br.com.jpbassinello.sbcgg.grpc.interfaces.users.User PROTO_USER = br.com.jpbassinello.sbcgg.grpc.interfaces.users.User.newBuilder()
      .setId(USER_ID.toString())
      .setFirstName("João")
      .setLastName("Bassinello")
      .setEmail("joao@sbcgg.com")
      .setEmailVerified(true)
      .setMobilePhoneNumber("+5519987654321")
      .setMobilePhoneNumberVerified(true)
      .setActive(true)
      .addAllRoles(List.of(UserRole.USER_ROLE_ADMIN, UserRole.USER_ROLE_USER))
      .setTimeZoneId("America/Sao_Paulo")
      .build();

  @Mock
  private LoadUsersUseCase loadAccounts;
  @Mock
  private ManageUsersUseCase manageAccounts;

  @InjectMocks
  private UsersGrpcAdapter adapter;

  @Test
  void loadUser() {
    when(loadAccounts.loadUserById(USER_ID))
        .thenReturn(USER);

    var captor = ArgumentCaptor.forClass(LoadUserResponse.class);
    StreamObserver<LoadUserResponse> observer = mock(StreamObserver.class);

    adapter.loadUser(LoadUserRequest.newBuilder().setId(USER_ID.toString()).build(), observer);
    verify(observer).onNext(captor.capture());

    assertThat(captor.getValue())
        .usingRecursiveComparison()
        .isEqualTo(LoadUserResponse.newBuilder().setUser(PROTO_USER).build());
  }

  @Test
  void loadUserByEmail() {
    when(loadAccounts.loadUserByEmail(USER.getEmail()))
        .thenReturn(USER);

    var captor = ArgumentCaptor.forClass(LoadUserResponse.class);
    StreamObserver<LoadUserResponse> observer = mock(StreamObserver.class);

    adapter.loadUser(LoadUserRequest.newBuilder().setEmail(USER.getEmail()).build(), observer);
    verify(observer).onNext(captor.capture());

    assertThat(captor.getValue())
        .usingRecursiveComparison()
        .isEqualTo(LoadUserResponse.newBuilder().setUser(PROTO_USER).build());
  }

  @Test
  void registerUser() {
    when(manageAccounts.register(
        ManageUsersUseCase.RegisterUserInput.builder()
            .firstName("João")
            .lastName("Bassinello")
            .email("joao@sbcgg.com")
            .mobilePhoneNumber("+5519987654321")
            .password("password")
            .roles(List.of(Role.ADMIN, Role.USER))
            .timeZoneId("America/Sao_Paulo")
            .build()
    )).thenReturn(USER);

    var captor = ArgumentCaptor.forClass(RegisterUserResponse.class);
    StreamObserver<RegisterUserResponse> observer = mock(StreamObserver.class);
    adapter.registerUser(
        RegisterUserRequest.newBuilder()
            .setInput(
                UserInput.newBuilder()
                    .setFirstName("João")
                    .setLastName("Bassinello")
                    .setEmail("joao@sbcgg.com")
                    .setMobilePhoneNumber("+5519987654321")
                    .addAllRoles(List.of(UserRole.USER_ROLE_ADMIN, UserRole.USER_ROLE_USER))
                    .setPassword("password")
                    .setTimeZoneId("America/Sao_Paulo")
                    .build()
            )
            .build(), observer
    );

    verify(observer).onNext(captor.capture());

    assertThat(captor.getValue())
        .usingRecursiveComparison()
        .isEqualTo(RegisterUserResponse.newBuilder().setUser(PROTO_USER).build());
  }

  @Test
  void searchUsers() {
    when(loadAccounts.searchUsers("terms", 0, 1))
        .thenReturn(new SimplePage<User>(List.of(USER), true));

    var captor = ArgumentCaptor.forClass(SearchUsersResponse.class);
    StreamObserver<SearchUsersResponse> observer = mock(StreamObserver.class);

    adapter.searchUsers(SearchUsersRequest.newBuilder().setTerms("terms").setPage(0).setPageSize(1).build(), observer);
    verify(observer).onNext(captor.capture());

    assertThat(captor.getValue())
        .usingRecursiveComparison()
        .isEqualTo(SearchUsersResponse.newBuilder().addUsers(PROTO_USER).setHasNext(true).build());
  }
}

package br.com.jpbassinello.sbcgg.services.messages.adapter.out.users;

import br.com.jpbassinello.sbcgg.grpc.interfaces.users.LoadUserRequest;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.LoadUserResponse;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.User;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.UserRole;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.UsersServiceGrpc;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserGrpcAdapterTest {

  @Mock
  private UsersServiceGrpc.UsersServiceBlockingStub stub;

  @InjectMocks
  private UserGrpcAdapter adapter;

  @Test
  void loadUserById() {

    var userId = UUID.randomUUID();

    when(stub.loadUser(LoadUserRequest.newBuilder().setId(userId.toString()).build()))
        .thenReturn(
            LoadUserResponse.newBuilder()
                .setUser(
                    User.newBuilder()
                        .setId(userId.toString())
                        .setEmail("email@email.com")
                        .setEmailVerified(true)
                        .setMobilePhoneNumber("+5519991038010")
                        .setMobilePhoneNumberVerified(false)
                        .setFirstName("John")
                        .setLastName("Doe")
                        .setActive(true)
                        .setTimeZoneId("America/Los_Angeles")
                        .addRoles(UserRole.USER_ROLE_USER)
                        .build()
                )
                .build()
        );

    var response = adapter.loadUserById(userId);

    assertThat(response.orElseThrow())
        .usingRecursiveComparison()
        .isEqualTo(
            br.com.jpbassinello.sbcgg.services.messages.domain.entities.User.builder()
                .id(userId)
                .email("email@email.com")
                .emailVerified(true)
                .mobilePhoneNumber("+5519991038010")
                .mobilePhoneNumberVerified(false)
                .firstName("John")
                .lastName("Doe")
                .build()
        );
  }

}

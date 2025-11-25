package br.com.jpbassinello.sbcgg.graphql.gateway.adapter.out.user;

import br.com.jpbassinello.sbcgg.graphql.gateway.domain.enums.Role;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.enums.UserContactMethod;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.inputs.RegisterUserInput;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.types.User;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.RegisterUserRequest;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.RegisterUserResponse;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.UserInput;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.UserRole;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.UsersServiceGrpc;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.VerifyContactMethodRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserGrpcAdapterTest {

  private static final UUID USER_ID = UUID.randomUUID();

  @Mock
  private UsersServiceGrpc.UsersServiceBlockingStub usersGrpc;

  @InjectMocks
  private UserGrpcAdapter adapter;

  @Test
  void register() {
    when(usersGrpc.registerUser(
            RegisterUserRequest.newBuilder()
                .setInput(
                    UserInput.newBuilder()
                        .setFirstName("John")
                        .setLastName("Doe")
                        .setEmail("john.doe@email.com")
                        .setMobilePhoneNumber("+5519991038010")
                        .setPassword("password")
                        .setTimeZoneId("America/Sao_Paulo")
                        .addRoles(UserRole.USER_ROLE_ADMIN)
                        .addRoles(UserRole.USER_ROLE_USER)
                        .build()
                ).build()
        )
    ).thenReturn(
        RegisterUserResponse.newBuilder()
            .setUser(
                br.com.jpbassinello.sbcgg.grpc.interfaces.users.User.newBuilder()
                    .setId(USER_ID.toString())
                    .setFirstName("John")
                    .setLastName("Doe")
                    .setEmail("john.doe@email.com")
                    .setEmailVerified(true)
                    .setMobilePhoneNumber("+5519991038010")
                    .setMobilePhoneNumberVerified(true)
                    .setTimeZoneId("America/Sao_Paulo")
                    .setActive(true)
                    .addAllRoles(List.of(UserRole.USER_ROLE_ADMIN, UserRole.USER_ROLE_USER))
                    .build()
            ).build()
    );

    var response = adapter.register(
        RegisterUserInput.builder()
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@email.com")
            .mobilePhoneNumber("+5519991038010")
            .password("password")
            .timeZoneId("America/Sao_Paulo")
            .build(), List.of(Role.ADMIN, Role.USER)
    );

    assertThat(response)
        .usingRecursiveComparison()
        .isEqualTo(
            User.builder()
                .id(USER_ID)
                .active(true)
                .email("john.doe@email.com")
                .emailVerified(true)
                .mobilePhoneNumber("+5519991038010")
                .mobilePhoneNumberVerified(true)
                .firstName("John")
                .lastName("Doe")
                .roles(List.of(Role.ADMIN, Role.USER))
                .timeZoneId("America/Sao_Paulo")
                .build()
        );
  }

  @Test
  void verifyUserContactMethod() {
    adapter.verifyUserContactMethod(USER_ID, "1234", UserContactMethod.MOBILE_PHONE_NUMBER);

    verify(usersGrpc).verifyContactMethod(
        VerifyContactMethodRequest.newBuilder()
            .setUserId(USER_ID.toString())
            .setCode("1234")
            .setMethod(br.com.jpbassinello.sbcgg.grpc.interfaces.users.UserContactMethod.USER_CONTACT_METHOD_MOBILE_PHONE_NUMBER)
            .build()
    );
  }

}

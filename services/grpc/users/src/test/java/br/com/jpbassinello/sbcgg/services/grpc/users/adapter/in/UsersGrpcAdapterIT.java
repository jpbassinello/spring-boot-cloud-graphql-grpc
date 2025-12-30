package br.com.jpbassinello.sbcgg.services.grpc.users.adapter.in;

import br.com.jpbassinello.sbcgg.grpc.interfaces.users.LoadUserRequest;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.RegisterUserRequest;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.UserInput;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.UserRole;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.UsersServiceGrpc;
import br.com.jpbassinello.sbcgg.services.grpc.users.adapter.PostgresContainer;
import br.com.jpbassinello.sbcgg.services.grpc.users.application.port.out.SendMessagePort;
import br.com.jpbassinello.sbcgg.services.grpc.users.application.port.out.SyncIdentityPort;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.grpc.test.autoconfigure.AutoConfigureInProcessTransport;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.context.annotation.Bean;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
@AutoConfigureInProcessTransport
@ImportTestcontainers(PostgresContainer.class)
class UsersGrpcAdapterIT {

  @MockitoBean
  private SyncIdentityPort syncIdentityPort;

  @MockitoBean
  private SendMessagePort sendMessage;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UsersServiceGrpc.UsersServiceBlockingStub grpcStub;

  @Test
  void loadUserExpectingNotFound() {
    var userId = UUID.randomUUID();
    var request = LoadUserRequest.newBuilder()
        .setId(userId.toString())
        .build();

    assertThatThrownBy(() -> grpcStub.loadUser(request))
        .isInstanceOf(StatusRuntimeException.class)
        .hasMessageContaining("NOT_FOUND: Resource not found")
        .matches(exception -> {
          var metadata = ((StatusRuntimeException) exception).getTrailers();
          return "user".equals(metadata.get(Metadata.Key.of("type", Metadata.ASCII_STRING_MARSHALLER)))
              && userId.toString().equals(metadata.get(Metadata.Key.of("id", Metadata.ASCII_STRING_MARSHALLER)));
        });
  }

  @Test
  void registerAdminExpectingConstraintViolations() {
    var request = RegisterUserRequest.newBuilder()
        .setInput(
            UserInput.newBuilder()
                .setFirstName("John")
                .setTimeZoneId("America/Sao_Paulo")
                .addRoles(UserRole.USER_ROLE_USER)
                .addRoles(UserRole.USER_ROLE_ADMIN)
                .build()
        )
        .build();

    assertThatThrownBy(() -> grpcStub.registerUser(request))
        .isInstanceOf(StatusRuntimeException.class)
        .hasMessageContaining("INVALID_ARGUMENT: Constraint violation")
        .matches(exception -> {
          var metadata = ((StatusRuntimeException) exception).getTrailers();

          try {
            List<String> violations = objectMapper.readValue(
                metadata.get(Metadata.Key.of("violations", Metadata.ASCII_STRING_MARSHALLER)),
                new TypeReference<>() {});
            return new HashSet<>(violations)
                .equals(Set.of("user.lastName.NotEmpty", "user.email.NotEmpty", "user.password.NotEmpty", "user.mobilePhoneNumber.NotEmpty"));
          } catch (JacksonException e) {
            return false;
          }
        });
  }

  @TestConfiguration
  static class Config {

    @Bean
    UsersServiceGrpc.UsersServiceBlockingStub grpcStub(GrpcChannelFactory channels) {
      return UsersServiceGrpc.newBlockingStub(channels.createChannel("local"));
    }

  }
}

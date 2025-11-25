package br.com.jpbassinello.sbcgg.services.grpc.users.adapter.in;

import br.com.jpbassinello.sbcgg.grpc.interfaces.users.LoadUserRequest;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.RegisterUserRequest;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.UserInput;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.UserRole;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.UsersServiceGrpc;
import br.com.jpbassinello.sbcgg.services.grpc.users.application.port.out.SendMessagePort;
import br.com.jpbassinello.sbcgg.services.grpc.users.application.port.out.SyncIdentityPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.autoconfigure.GrpcAdviceAutoConfiguration;
import net.devh.boot.grpc.server.autoconfigure.GrpcServerAutoConfiguration;
import net.devh.boot.grpc.server.autoconfigure.GrpcServerFactoryAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ImportAutoConfiguration({
    GrpcAdviceAutoConfiguration.class,
    GrpcServerAutoConfiguration.class,
    GrpcServerFactoryAutoConfiguration.class,
    GrpcClientAutoConfiguration.class
})
@ActiveProfiles("test")
@DirtiesContext
class UsersGrpcAdapterIT {

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.7-alpine3.22")
      .withDatabaseName("messages")
      .withUsername("postgres")
      .withPassword("postgres");

  @GrpcClient("inProcess")
  private UsersServiceGrpc.UsersServiceBlockingStub grpcStub;

  @MockitoBean
  private SyncIdentityPort syncIdentityPort;

  @MockitoBean
  private SendMessagePort sendMessage;

  @Autowired
  private ObjectMapper objectMapper;

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
          } catch (JsonProcessingException e) {
            return false;
          }
        });
  }
}

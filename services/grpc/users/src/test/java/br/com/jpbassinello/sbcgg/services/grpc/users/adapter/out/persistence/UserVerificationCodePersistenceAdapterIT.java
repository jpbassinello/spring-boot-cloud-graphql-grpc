package br.com.jpbassinello.sbcgg.services.grpc.users.adapter.out.persistence;

import br.com.jpbassinello.sbcgg.services.grpc.users.config.PersistenceConfig;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.entities.User;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.entities.UserVerificationCode;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.enums.Role;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.enums.UserVerificationCodeType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.TestDatabaseAutoConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(excludeAutoConfiguration = {TestDatabaseAutoConfiguration.class})
@ContextConfiguration(classes = {PersistenceConfig.class})
@ActiveProfiles("test")
class UserVerificationCodePersistenceAdapterIT {

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18.1-alpine3.23")
      .withDatabaseName("users")
      .withUsername("postgres")
      .withPassword("postgres");

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserVerificationCodePersistenceAdapter adapter;

  @Test
  void checkExpired() {
    var user = userRepository.save(
        User.builder()
            .firstName("John")
            .lastName("Doe")
            .email("john@sbcgg.com")
            .mobilePhoneNumber("+5519991038012")
            .roles(List.of(Role.USER))
            .registeredAt(ZonedDateTime.now())
            .build()
    );

    var now = ZonedDateTime.now();
    adapter.save(
        UserVerificationCode.builder()
            .userId(user.getId())
            .code("123456")
            .source("john@sbcgg.com")
            .type(UserVerificationCodeType.EMAIL)
            .validUntil(now.minusNanos(1))
            .build()
    );

    assertThat(
        adapter.isValid(user.getId(), "123456", UserVerificationCodeType.EMAIL, now)
    ).isEmpty();
  }

  @Test
  void checkValid() {
    var user = userRepository.save(
        User.builder()
            .firstName("John")
            .lastName("Doe")
            .email("john@sbcgg.com")
            .mobilePhoneNumber("+5519991038012")
            .roles(List.of(Role.USER))
            .registeredAt(ZonedDateTime.now())
            .build()
    );

    var now = ZonedDateTime.now();
    adapter.save(
        UserVerificationCode.builder()
            .userId(user.getId())
            .code("123456")
            .source("john@sbcgg.com")
            .type(UserVerificationCodeType.EMAIL)
            .validUntil(now.plusSeconds(5))
            .build()
    );

    assertThat(
        adapter.isValid(user.getId(), "123456", UserVerificationCodeType.EMAIL, now)
    ).isPresent();
  }

}

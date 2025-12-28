package br.com.jpbassinello.sbcgg.services.grpc.users.application.services;

import br.com.jpbassinello.sbcgg.services.grpc.users.config.PersistenceConfig;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.TestDatabaseAutoConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@DataJpaTest(excludeAutoConfiguration = {TestDatabaseAutoConfiguration.class})
@ContextConfiguration(classes = {ValidationAutoConfiguration.class, PersistenceConfig.class}) // enabling validation
@ActiveProfiles("test")
abstract class BaseServiceIT {

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18.1-alpine3.23")
      .withDatabaseName("users")
      .withUsername("postgres")
      .withPassword("postgres");
}

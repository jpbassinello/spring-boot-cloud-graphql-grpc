package br.com.jpbassinello.sbcgg.services.grpc.users.adapter;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

public interface PostgresContainer {

  @Container
  @ServiceConnection
  PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18.1-alpine3.23")
      .withDatabaseName("users")
      .withUsername("postgres")
      .withPassword("postgres");

}
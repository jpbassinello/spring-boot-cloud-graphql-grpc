package br.com.jpbassinello.sbcgg.services.grpc.users.adapter;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;

public interface PostgresContainer {

  @Container
  @ServiceConnection
  PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18.2-alpine3.23")
      .withDatabaseName("users")
      .withUsername("postgres")
      .withPassword("postgres");

}
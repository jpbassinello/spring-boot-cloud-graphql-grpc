package br.com.jpbassinello.sbcgg.services.messages.adapter.out.persistence;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;

interface PostgresContainer {

  @Container
  @ServiceConnection
  PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18.2-alpine3.23")
      .withDatabaseName("messages")
      .withUsername("postgres")
      .withPassword("postgres");

}
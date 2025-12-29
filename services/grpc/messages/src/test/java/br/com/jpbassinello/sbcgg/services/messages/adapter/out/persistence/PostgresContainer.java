package br.com.jpbassinello.sbcgg.services.messages.adapter.out.persistence;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

interface PostgresContainer {

  @Container
  PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18.1-alpine3.23")
      .withDatabaseName("messages")
      .withUsername("postgres")
      .withPassword("postgres");

}
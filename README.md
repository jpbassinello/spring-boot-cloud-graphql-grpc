# Spring Boot Cloud GraphQL gRPC (SBCGG)

> A production-ready, enterprise-grade microservices platform built with Spring Boot, gRPC, and GraphQL

[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring gRPC](https://img.shields.io/badge/Spring%20gRPC-1.0.2-blue.svg)](https://docs.spring.io/spring-grpc/reference/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

## Table of Contents

- [Motivation](#motivation)
- [Overview](#overview)
- [Use Cases](#use-cases)
- [Architecture](#architecture)
- [Key Features](#key-features)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Building the Project](#building-the-project)
    - [Running Services](#running-services)
- [Project Structure](#project-structure)
- [Testing](#testing)
- [Development](#development)
    - [Code Style](#code-style)
    - [Adding New Services](#adding-new-services)
- [Monitoring & Observability](#monitoring--observability)
- [Documentation](#documentation)
- [Contributing](#contributing)
- [License](#license)

## Motivation

As a developer, we understand the value of active communities in our day-by-day activities. Given something back
to the developers out there has always been part of my career plans and this project was built with this mentality.

The main idea of this project, even before I decided to publish it publicly, was to help with my own studies. Every time
I had an interest in a different technology or library, I tried to include it in this repository. For example, as a
Maven enthusiast, I've never applied Gradle in a real project before. Same situation for Kubernetes.

My plan is to keep sbcgg up-to-date with modern components available. I am open to suggestions for new features and
different approaches for the project. Feel free to contribute to sbcgg.

## Overview

SBCGG (Spring Boot Cloud GraphQL gRPC) is a modern, cloud-native microservices platform that demonstrates
enterprise-grade architecture patterns and best practices. Built with **Spring Boot 4.0.3** and **Spring Cloud 2025.1.1
**, it provides a scalable, resilient foundation for distributed systems.

### Cloud Agnostic

SBCGG is designed to be **cloud agnostic**, meaning it can run on any cloud provider (AWS, GCP, Azure) or on-premises
infrastructure without vendor lock-in. The platform achieves this through:

- **Kubernetes-native deployment**: Standard Kubernetes manifests that work across any certified Kubernetes distribution
- **Open-source infrastructure**: Using vendor-neutral technologies like PostgreSQL, Redis, Consul, and Keycloak
- **No proprietary cloud services**: All components are replaceable open-source alternatives to cloud-specific services
- **Container-based architecture**: Docker images that run consistently across any container runtime

This approach gives you the flexibility to migrate between cloud providers, adopt hybrid or multi-cloud strategies, or
run entirely on-premises while maintaining the same application code and deployment configurations.

### Industry Adoption

This project leverages battle-tested technologies used by industry leaders:

- **Netflix** pioneered many of the components used in this stack and has been a major contributor to Spring Cloud.
  Netflix continues to use Spring Boot, gRPC, and GraphQL in their microservices architecture to handle billions of
  views seamlessly.

- **gRPC** is used by major tech companies including **Uber** (for ride-hailing services), **Square** (payment
  processing), **Netflix** (streaming services), **Spotify** (music streaming), **Dropbox** (file storage), and **Cisco
  ** (network management).

- **GraphQL** has been adopted by **GitHub** (API platform), **Shopify** (e-commerce), **PayPal** (50+ products), *
  *Twitter** (mobile and web apps), **Airbnb** (booking platform), and **The New York Times** (content delivery).

- **HashiCorp Consul** is used by enterprises including **Criteo**, **Citadel**, and **Q2** for service discovery,
  configuration management, and service mesh capabilities in cloud-native architectures.

## Use Cases

Considering what is common to my projects, the following use cases are implemented in this project aiming to be a
reusable source:

* As a user, I would like to register in the system
* As an admin, I would like to register a user in the system
* As a user, I would like to log in into the system
* As an admin, I would like to send a message to a user
* As an admin, I would like to see all the messages of a user

## Architecture

SBCGG follows a **hexagonal (ports and adapters)** architecture pattern combined with **domain-driven design (DDD)**
principles:

```
                    ┌─────────────────┐
                    │  GraphQL Gateway│
                    │  (API Gateway)  │
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │     Consul      │
                    │   (Discovery &  │
                    │  Configuration) │
                    └────────┬────────┘
                             │
              ┌──────────────┴──────────────┐
              ▼                             ▼
    ┌─────────────────┐          ┌─────────────────┐
    │  Users Service  │◄────────►│ Messages Service│
    │     (gRPC)      │  gRPC    │     (gRPC)      │
    └────────┬────────┘          └────────┬────────┘
             │                            │
             ▼                            ▼
    ┌─────────────────┐          ┌─────────────────┐
    │  PostgreSQL DB  │          │  PostgreSQL DB  │
    │    (Users)      │          │   (Messages)    │
    └─────────────────┘          └─────────────────┘

        Shared Infrastructure (All services use):
        ┌─────────────────┬─────────────────┬─────────────────┐
        │   Redis Cache   │    Keycloak     │ Grafana LGTM    │
        │    (Shared)     │     (Auth)      │ (Observability) │
        └─────────────────┴─────────────────┴─────────────────┘
```

### Core Design Principles

- **Microservices Architecture**: Independent, loosely coupled services
- **Centralized Configuration**: HashiCorp Consul KV store for externalized configuration
- **Service Discovery**: Dynamic service registration and discovery with HashiCorp Consul
- **API Gateway Pattern**: Unified entry point through GraphQL gateway
- **Event-Driven Communication**: Asynchronous messaging between services
- **Database per Service**: Each microservice owns its data
- **Distributed Tracing**: OpenTelemetry with Grafana LGTM stack for observability
- **Circuit Breaker**: Resilience patterns for fault tolerance

## Key Features

- **Multi-Protocol Support**: REST, gRPC, and GraphQL APIs
- **Centralized Configuration**: HashiCorp Consul KV store for external configuration management
- **Service Discovery**: Automatic service registration with HashiCorp Consul
- **Distributed Tracing & Logging**: OpenTelemetry with Grafana LGTM stack (Loki, Grafana, Tempo, Mimir)
- **Distributed Cache**: Redis for shared caching across services
- **Database Migrations**: Flyway for version-controlled schema management
- **Security**: Keycloak integration for authentication and authorization
- **RBAC**: Role Based Access Control
- **Resilience**: Circuit breakers, retries, and timeouts
- **Monitoring**: Spring Boot Actuator with Micrometer metrics
- **Code Quality**: Checkstyle, SpotBugs, and JaCoCo integration
- **Type-Safe Mapping**: MapStruct for object mapping
- **Distributed Locking**: ShedLock for scheduled task coordination

## Technology Stack

### Core Framework

- [Spring Boot](https://spring.io/projects/spring-boot) 4.0.3 - Application framework
- [Spring Cloud](https://spring.io/projects/spring-cloud) 2025.1.1 - Microservices toolkit
- [Spring Security](https://spring.io/projects/spring-security) 7.0.3 - Security framework
- [Java](https://openjdk.java.net/) 25 (LTS) - Programming language

### Communication & Infrastructure

- [Spring gRPC](https://docs.spring.io/spring-grpc/reference/) 1.0.2 - High-performance RPC framework
- [Protocol Buffers](https://developers.google.com/protocol-buffers) 4.33.4 - Serialization format
- [GraphQL Java](https://www.graphql-java.com/) - GraphQL implementation
- [Spring Cloud Consul](https://docs.spring.io/spring-cloud-consul/reference/) - Service discovery and configuration
- [HashiCorp Consul](https://www.consul.io/) 1.22.5 - Service mesh, discovery, and configuration

### Data & Persistence

- [Spring Data JPA](https://spring.io/projects/spring-data-jpa) - Data access layer
- [PostgreSQL](https://www.postgresql.org/) 18.3 - Relational database
- [HikariCP](https://github.com/brettwooldridge/HikariCP) - JDBC connection pool
- [Flyway](https://flywaydb.org/) - Database migration tool
- [Redis](https://redis.io/) 8.6.1 - Distributed cache

### Observability & Monitoring

- [OpenTelemetry](https://opentelemetry.io/) 2.25.0 - Distributed tracing and logging
- [Grafana LGTM Stack](https://grafana.com/docs/opentelemetry/docker-lgtm/) 0.19.1 - Loki (logs), Grafana (
  visualization), Tempo (traces), Mimir (metrics)
- [Micrometer](https://micrometer.io/) - Application metrics
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html) -
  Production-ready features

### Security & Authentication

- [Keycloak](https://www.keycloak.org/) 26.5.4 - Identity and access management

### Developer Tools

- [MapStruct](https://mapstruct.org/) 1.6.3 - Type-safe bean mapping
- [Lombok](https://projectlombok.org/) - Boilerplate reduction
- [ShedLock](https://github.com/lukas-krecan/ShedLock) 7.6.0 - Distributed lock

### Build & Quality

- [Gradle](https://gradle.org/) 9.2.1 - Build automation
- [Checkstyle](https://checkstyle.org/) - Code style enforcement
- [SpotBugs](https://spotbugs.github.io/) - Static analysis
- [JaCoCo](https://www.jacoco.org/jacoco/) - Code coverage

### Testing

- [JUnit 5](https://junit.org/junit5/) - Testing framework
- [AssertJ](https://assertj.github.io/doc/) - Fluent assertions
- [Mockito](https://site.mockito.org/) - Mocking framework
- [Spring Boot Test](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing) -
  Integration testing

## Getting Started

### Prerequisites

Before you begin, ensure you have the following installed:

- **Java 25 (LTS)** - [Download](https://adoptium.net/)
- **Gradle 9.2.1** (wrapper included in the project)
- **Docker & Docker Compose** (optional, for running dependencies)
- **PostgreSQL 18+** (if not using Docker)
- **Redis 8+** (if not using Docker)

### Building the Project

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/sbcgg.git
   cd sbcgg
   ```

2. **Build the entire project**
   ```bash
   ./gradlew build
   ```

3. **Build a specific module**
   ```bash
   ./gradlew :shared:util:build
   ```

4. **Skip tests during build**
   ```bash
   ./gradlew build -x test
   ```

### Running Services

#### Option 1: Local Development (Recommended for Development)

Services run locally with minimal dependencies. Consul is disabled by default.

1. **Start the shared infrastructure services** (PostgreSQL, Redis, Keycloak)
   ```bash
   cd infrastructure/docker/compose/shared
   docker-compose up -d
   cd -
   ```

   Access points:
    - Keycloak admin console: `http://localhost:9090`

2. **Start gRPC services**
   ```bash
   # Users service (port 8090/8091)
   ./gradlew :services:grpc:users:bootRun

   # Messages service (port 8190/8191)
   ./gradlew :services:grpc:messages:bootRun
   ```

3. **Start GraphQL Gateway**
   ```bash
   ./gradlew :services:graphql:gateway:bootRun
   ```
   Access GraphQL playground at: `http://localhost:8080/graphiql`

**Note**: With the `dev` profile (default), services use local configuration files and communicate via localhost. Consul
integration is disabled for faster startup.

#### Option 2: Docker Environment (Full Consul Integration)

All services run in Docker with full Consul integration for service discovery and configuration.

1. **Build Docker images**
   ```bash
   ./gradlew build docker
   ```

2. **Start all services**
   ```bash
   cd infrastructure/docker/compose/dev
   docker-compose up -d
   ```

   Access points:
    - Consul UI: `http://localhost:8500` (optional)
    - Keycloak admin console: `http://localhost:9090`
    - Grafana (LGTM): `http://localhost:3100`

Services will use the `dev-docker` profile, which enables:

- Consul-based service discovery
- Consul KV store for configuration
- Service-to-service communication via Consul

3. **GraphQL Gateway**
   Access GraphQL playground at: `http://localhost:8080/graphiql`

#### Option 3: Kubernetes Deployment (Cloud Agnostic)

Deploy to any Kubernetes cluster (Minikube, Kind, Docker Desktop, GKE, EKS, AKS, or on-premises).

1. **Prerequisites**
   ```bash
   # Ensure kubectl is installed
   kubectl version --client

   # For local development, enable ingress addon (Minikube example)
   minikube addons enable ingress
   minikube addons enable metrics-server
   ```

2. **Build Docker images**
   ```bash
   ./gradlew clean build bootBuildImage --info
   ```

3. **Deploy to Kubernetes**
   ```bash
   cd infrastructure/kubernetes

   # Deploy all resources using kustomize
   kubectl apply -k .

   # Watch pods come up
   kubectl get pods -n sbcgg -w
   ```

4. **Configure local DNS** (for local development)

   Add to `/etc/hosts`:
   ```
   127.0.0.1 sbcgg.local
   127.0.0.1 grafana.sbcgg.local
   127.0.0.1 consul.sbcgg.local
   127.0.0.1 keycloak.sbcgg.local
   ```

5. **Access services**
    - GraphQL API: `http://sbcgg.local/graphql`
    - GraphiQL: `http://sbcgg.local/graphiql`
    - Grafana: `http://grafana.sbcgg.local`
    - Consul UI: `http://consul.sbcgg.local`
    - Keycloak: `http://keycloak.sbcgg.local`

For detailed Kubernetes deployment instructions, including production considerations, scaling, and troubleshooting, see
the [Kubernetes README](infrastructure/kubernetes/README.md).

## Project Structure

The project follows a modular, multi-module Gradle structure:

```
sbcgg/
├── grpc-interfaces/          # gRPC service definitions
│   ├── messages/            # Message service proto files
│   └── users/               # User service proto files
│
├── services/                 # Service implementations
│   ├── graphql/            # GraphQL gateway
│   │   └── gateway/        # GraphQL API gateway
│   └── grpc/               # gRPC service implementations
│       ├── messages/       # Messages microservice
│       └── users/          # Users microservice
│
├── shared/                   # Shared libraries and utilities
│   ├── exception/          # Common exception handling
│   ├── grpc-client/        # gRPC client utilities
│   ├── grpc-server/        # gRPC server utilities
│   ├── mapping/            # Object mapping utilities
│   ├── proto-mapping/      # Protobuf mapping utilities
│   ├── spring-app/         # Spring application base
│   ├── spring-cache/       # Caching configuration
│   ├── spring-jpa/         # JPA/database utilities
│   ├── spring-shedlock/    # Distributed locking
│   ├── util/               # General utilities
│   └── validation/         # Validation utilities
│
├── tests/                    # Test modules
│   └── e2e/                 # End-to-end tests
│
├── infrastructure/           # Infrastructure setup
│   └── docker/
│       └── compose/
│           ├── configs/    # Consul, Keycloak, PostgreSQL configs
│           ├── dev/        # Development Docker Compose
│           └── shared/     # Shared Docker services
│
├── config/                   # Build tool configurations
│   ├── checkstyle/         # Checkstyle rules
│   └── spotbugs/           # SpotBugs exclusions
│
├── build.gradle.kts          # Root build configuration
├── settings.gradle.kts       # Multi-module settings
└── gradle.properties         # Gradle properties
```

### Module Organization

- **grpc-interfaces**: Protocol Buffer definitions and generated code
- **services**: Independent microservices
    - **grpc**: gRPC microservices (users, messages)
    - **graphql**: GraphQL API gateway
- **shared**: Reusable libraries shared across services
- **tests**: Test modules (e2e tests)
- **infrastructure**: Docker Compose configurations, Consul configs, and infrastructure setup

### Hexagonal Architecture Structure (per service)

```
service/
├── adapter/
│   ├── in/                  # Input adapters (controllers, gRPC endpoints)
│   └── out/                 # Output adapters (repositories, external APIs)
├── application/
│   ├── port/
│   │   ├── in/             # Input ports (use cases)
│   │   └── out/            # Output ports (interfaces)
│   └── service/            # Use case implementations
└── domain/                  # Domain models and business logic
```

## Testing

### Running Tests

```bash
# Run all tests
./gradlew test

# Run tests for a specific module
./gradlew :shared:util:test

# Run a specific test class
./gradlew :shared:util:test --tests "br.com.jpbassinello.sbcgg.utils.DateTimeUtilsTest"

# Run a specific test method
./gradlew :shared:util:test --tests "br.com.jpbassinello.sbcgg.utils.DateTimeUtilsTest.isFutureDate"

# Generate coverage report
./gradlew jacocoTestReport
```

### Test Types

1. **Unit Tests** (`*Test.java`)
    - Test individual components in isolation
    - Use JUnit 5 and AssertJ
    - Fast execution, no external dependencies

2. **Integration Tests** (`*IT.java`)
    - Test components with their dependencies
    - Use Spring Boot Test framework
    - May require test containers or embedded services

### Writing Tests

#### Unit Test Example

```java
package br.com.jpbassinello.sbcgg.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MyUtilTest {
  @Test
  void testMyMethod() {
    // Arrange
    var input = "test";

    // Act
    var result = MyUtil.myMethod(input);

    // Assert
    assertThat(result).isEqualTo("expected");
  }
}
```

#### Integration Test Example

```java
package br.com.jpbassinello.sbcgg.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = {MyService.class})
class MyServiceIT extends BaseServiceIT {
  @MockBean
  private ExternalDependency dependency;

  @Autowired
  private MyService service;

  @Test
  void testServiceMethod() {
    // Arrange, Act, Assert
    var result = service.process(new ServiceInput("test"));
    assertThat(result.getValue()).isEqualTo("processed");
  }
}
```

## Development

### Code Style

- **Checkstyle**: Enforces Google Java Style Guide
- **SpotBugs**: Identifies potential bugs (currently disabled for Java 25)
- **Lombok**: Reduces boilerplate code
- **MapStruct**: Type-safe bean mapping

### Adding New Services

1. **Create gRPC interface** in `grpc-interfaces/`
2. **Define Protocol Buffer** schema
3. **Implement service** in `services/grpc/`
4. **Add hexagonal structure**: adapters, application, domain
5. **Configure service**: Add configuration files in `src/main/resources/config/`
6. **Add Consul configuration** (for Docker deployment): Create YAML file in
   `infrastructure/docker/compose/configs/consul/yaml/`
7. **Configure Consul registration** via Spring Cloud Consul (enabled in `dev-docker` profile)
8. **Add integration tests**

### Development Best Practices

- Follow the Arrange-Act-Assert pattern in tests
- Use descriptive test method names
- Mock external dependencies in tests
- Keep services small and focused
- Use Spring profiles for environment-specific configuration
- Document public APIs with JavaDoc
- Use SLF4J for logging

### Gradle Build Optimization

The project uses Gradle build cache for faster builds:

```properties
org.gradle.caching=true
org.gradle.parallel=true
org.gradle.configureondemand=true
```

To verify cache performance:

```bash
./gradlew clean build --scan
```

## Monitoring & Observability

### Health Checks

All services expose Spring Boot Actuator endpoints:

```bash
# Check service health
curl http://localhost:8080/actuator/health

# View metrics
curl http://localhost:8080/actuator/metrics

# View available endpoints
curl http://localhost:8080/actuator
```

### Distributed Tracing & Logging

OpenTelemetry integration with Grafana LGTM stack provides:

- **Traces**: Distributed tracing across all services via Tempo
- **Logs**: Centralized logging via Loki with OpenTelemetry appender (logback-spring.xml)
- **Metrics**: Performance metrics via Mimir
- **Visualization**: Unified dashboard via Grafana (http://localhost:3100)
- Trace context propagation across services
- Correlation IDs for request tracking

**Access Grafana**: http://localhost:3100

### Metrics

Micrometer collects metrics for:

- HTTP requests
- gRPC calls
- Database queries
- Cache hit/miss rates
- JVM memory and threads

## Documentation

- [CLAUDE.md](CLAUDE.md) - Development guidelines and instructions
- [API Documentation](#) - GraphQL schema and gRPC service definitions
- [Architecture Decision Records](#) - Design decisions and rationale

### Blog Posts

- [Introducing SBCGG](posts/1-About-SBCGG.md) - The story behind this project and why it was built

### Additional Resources

- [How to add Checkstyle and FindBugs plugins in a Gradle-based project](https://medium.com/@raveensr/how-to-add-checkstyle-and-findbugs-plugins-in-a-gradle-based-project-51759aa843be)

## Contributing

We welcome contributions! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Quality Requirements

- All tests must pass
- Code coverage must not decrease
- Checkstyle violations must be addressed
- Follow existing code style and patterns

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

---

**Built with** ❤️ **using Spring Boot, gRPC, and GraphQL**

For questions or support, please [open an issue](./issues).

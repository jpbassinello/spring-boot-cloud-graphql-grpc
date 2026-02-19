# SBCGG Development Guidelines

This document provides essential information for developers and AI assistants working on the SBCGG (Spring Boot Cloud
GraphQL gRPC) project. For general project information and public-facing documentation, see [README.md](README.md).

## Table of Contents

- [Quick Reference](#quick-reference)
- [Build & Configuration](#build--configuration)
- [Project Structure](#project-structure)
- [Testing](#testing)
- [Development Workflows](#development-workflows)
- [Code Quality & Static Analysis](#code-quality--static-analysis)
- [Architecture Patterns](#architecture-patterns)
- [Common Tasks](#common-tasks)
- [Troubleshooting](#troubleshooting)

## Quick Reference

### Essential Commands

```bash
# Build entire project
./gradlew build

# Run all tests
./gradlew test

# Start a gRPC service
./gradlew :services:grpc:users:bootRun

# Start GraphQL gateway
./gradlew :services:graphql:gateway:bootRun

# Generate code coverage report
./gradlew jacocoTestReport

# Check code style
./gradlew checkstyleMain checkstyleTest
```

### Key URLs (Local Development)

- **Consul Dashboard**: http://localhost:8500 (Service discovery & configuration)
- **Keycloak Admin**: http://localhost:9090
- **Grafana Dashboard**: http://localhost:3100 (LGTM: Loki, Grafana, Tempo, Mimir)
- **GraphQL Playground**: http://localhost:8080/graphiql
- **Actuator Health**: http://localhost:8080/actuator/health
- **Users Service**: http://localhost:8090 (gRPC: 8091)

## Build & Configuration

### Prerequisites

- **Java 25 (LTS)** - Required for building and running the project
- **Gradle 9.2.1** - Wrapper included in the project
- **Docker & Docker Compose** - For running infrastructure services (PostgreSQL, Redis, Keycloak)

### Building the Project

#### Full Build

```bash
./gradlew build
```

#### Module-Specific Build

```bash
./gradlew :shared:util:build
./gradlew :services:grpc:users:build
```

#### Skip Tests

```bash
./gradlew build -x test
```

#### Clean Build

```bash
./gradlew clean build
```

### Dependency Management

The project uses **Gradle Version Catalogs** (defined in `settings.gradle.kts`) for centralized dependency management:

```kotlin
// Version Catalog Structure
libs.versions.spring.security = "7.0.2"
libs.versions.spring.cloud = "2025.1.1"
libs.versions.spring.grpc = "1.0.2"
libs.versions.protoc.protobuf = "4.33.2"
libs.plugins.spring.boot = "4.0.2"
```

### Key Dependencies

| Category          | Library         | Version  | Purpose                           |
|-------------------|-----------------|----------|-----------------------------------|
| Framework         | Spring Boot     | 4.0.2    | Application framework             |
| Framework         | Spring Cloud    | 2025.1.1 | Microservices toolkit             |
| Service Discovery | Consul          | 1.22.3   | Service discovery & configuration |
| Security          | Spring Security | 7.0.2    | Security framework                |
| Security          | Keycloak        | 26.5.3   | Identity & access management      |
| RPC               | Spring gRPC     | 1.0.2    | High-performance RPC              |
| API               | GraphQL Java    | latest   | GraphQL implementation            |
| Database          | PostgreSQL      | 18.2     | Relational database               |
| Cache             | Redis           | 8.6.0    | Distributed cache                 |
| Mapping           | MapStruct       | 1.6.3    | Bean mapping                      |
| Observability     | OpenTelemetry   | 2.25.0   | Distributed tracing & logging     |
| Observability     | Grafana LGTM    | 0.18.1   | Loki, Grafana, Tempo, Mimir       |

## Project Structure

### Module Organization

The project follows a **multi-module Gradle structure** with three main groups:

```
sbcgg/
├── grpc-interfaces/          # Protocol Buffer definitions
│   ├── messages/            # Message service protos
│   └── users/               # User service protos
│
├── services/                 # Microservice implementations
│   ├── graphql/gateway/     # GraphQL API gateway
│   └── grpc/                # gRPC microservices
│       ├── messages/        # Messages service
│       └── users/           # Users service
│
├── shared/                   # Shared libraries
│   ├── exception/           # Exception handling
│   ├── grpc-client/         # gRPC client utilities
│   ├── grpc-server/         # gRPC server utilities
│   ├── mapping/             # Object mapping
│   ├── proto-mapping/       # Protobuf mapping
│   ├── spring-app/          # Spring base config
│   ├── spring-cache/        # Cache config (Redis)
│   ├── spring-jpa/          # JPA/database config
│   ├── spring-shedlock/     # Distributed locking
│   ├── util/                # General utilities
│   └── validation/          # Validation utilities
│
└── tests/                    # Test modules
    └── e2e/                 # End-to-end tests
```

### Hexagonal Architecture (Per Service)

Each microservice follows **hexagonal (ports and adapters)** architecture:

```
service/
├── domain/                   # Domain models & business logic
│   └── model/               # Domain entities
│
├── application/             # Application layer
│   ├── port/
│   │   ├── in/             # Input ports (use case interfaces)
│   │   └── out/            # Output ports (repository interfaces)
│   └── service/            # Use case implementations
│
└── adapter/                 # Adapters layer
    ├── in/                  # Input adapters
    │   ├── grpc/           # gRPC controllers
    │   └── rest/           # REST controllers
    └── out/                 # Output adapters
        ├── persistence/    # Database adapters
        └── external/       # External API clients
```

### Key Design Patterns

- **Hexagonal Architecture**: Clear separation of concerns
- **Domain-Driven Design (DDD)**: Domain models at the core
- **Dependency Inversion**: Domain layer has no dependencies
- **Repository Pattern**: Data access abstraction
- **Use Case Pattern**: Application services as use cases

## Testing

### Test Structure

The project uses **two types of tests**:

| Type              | Pattern      | Purpose                     | Speed  | Dependencies             |
|-------------------|--------------|-----------------------------|--------|--------------------------|
| Unit Tests        | `*Test.java` | Test individual components  | Fast   | None (mocked)            |
| Integration Tests | `*IT.java`   | Test with real dependencies | Slower | Spring context, DB, etc. |

### Running Tests

```bash
# All tests
./gradlew test

# Specific module
./gradlew :shared:util:test

# Specific test class
./gradlew :shared:util:test --tests "br.com.jpbassinello.sbcgg.utils.DateTimeUtilsTest"

# Specific test method
./gradlew :shared:util:test --tests "br.com.jpbassinello.sbcgg.utils.DateTimeUtilsTest.isFutureDate"

# With coverage report
./gradlew test jacocoTestReport
```

### Writing Unit Tests

**Template:**

```java
package br.com.jpbassinello.sbcgg.feature;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Feature Name Tests")
class FeatureTest {

  @Test
  @DisplayName("should perform expected behavior when given valid input")
  void shouldPerformExpectedBehavior() {
    // Arrange
    var input = "test";
    var sut = new Feature();

    // Act
    var result = sut.process(input);

    // Assert
    assertThat(result)
        .isNotNull()
        .isEqualTo("expected");
  }

  @Test
  @DisplayName("should throw exception when given invalid input")
  void shouldThrowExceptionWhenInvalid() {
    // Arrange
    var sut = new Feature();

    // Act & Assert
    assertThatThrownBy(() -> sut.process(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Input cannot be null");
  }
}
```

### Writing Integration Tests

**Template:**

```java
package br.com.jpbassinello.sbcgg.feature;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ContextConfiguration(classes = {FeatureService.class})
@ActiveProfiles("test")
class FeatureServiceIT extends BaseServiceIT {

  @MockBean
  private ExternalDependency externalDependency;

  @Autowired
  private FeatureService service;

  @Test
  void shouldCallExternalDependency() {
    // Arrange
    when(externalDependency.fetch(any())).thenReturn("mocked");
    var input = new ServiceInput("test");

    // Act
    var result = service.process(input);

    // Assert
    assertThat(result.getValue()).isEqualTo("processed");
    verify(externalDependency).fetch("test");
  }
}
```

### Testing Best Practices

✅ **Do:**

- Use **Arrange-Act-Assert** pattern consistently
- Write **descriptive test names** that explain the scenario
- Use `@DisplayName` for complex test descriptions
- Mock **external dependencies** in integration tests
- Use `@DataJpaTest` for repository tests
- Use `@ActiveProfiles("test")` for test-specific configuration
- Test **edge cases** and **error conditions**
- Keep tests **independent** and **idempotent**
- Prefer full object asserting with `usingRecursiveComparison()`

❌ **Don't:**

- Share state between tests
- Use hard-coded sleep/wait statements
- Test implementation details
- Create tests that depend on execution order
- Mix unit and integration test concerns

## Development Workflows

### Starting the Application Locally

**Important**: Services must be started in order due to dependencies.

**Step 1: Start Infrastructure**

```bash
cd infrastructure/docker/compose/dev
docker-compose up -d
cd -
```

This starts:

- **PostgreSQL 18.1** - Database
- **Redis 8.6.0** - Distributed cache
- **Consul 1.22.3** - Service discovery and configuration
- **Keycloak 26.5.3** - Authentication/Authorization
- **Grafana LGTM 0.17.1** - Observability stack (Loki, Grafana, Tempo, Mimir)

**Note**: Consul automatically loads service configurations from YAML files during initialization via the `init.sh`
script.

**Step 2: Verify Consul**
Consul starts automatically via Docker Compose. Verify it's running at http://localhost:8500

Check that configurations are loaded:

```bash
# Verify Consul KV store has configuration
curl http://localhost:8500/v1/kv/config/users-service,dev/data
```

**Step 3: Start Microservices**

```bash
# Terminal 1
./gradlew :services:grpc:users:bootRun

# Terminal 2
./gradlew :services:grpc:messages:bootRun
```

**Note**: When running locally (with `dev` profile), services do not connect to Consul by default. Consul integration is
enabled only with the `dev-docker` profile.

**Step 4: Start API Gateway**

```bash
./gradlew :services:graphql:gateway:bootRun
```

### Consul Configuration Management

Consul provides both service discovery and centralized configuration management using its Key/Value (KV) store.

**Configuration Storage**: Configuration files are stored in `infrastructure/docker/compose/configs/consul/yaml/`

**Configuration Files**:

- `users-service.yaml` - Users service configuration
- `messages-service.yaml` - Messages service configuration

**How It Works**:

1. During Docker Compose startup, Consul runs the `init.sh` script
2. The script loads YAML files into Consul's KV store using keys like `config/{service-name},{profile}/data`
3. Services connect to Consul and retrieve their configuration on startup

**Service Configuration**:
Services enable Consul config in `shared/spring-app/src/main/resources/config/app/application-dev-docker.yaml`:

```yaml
spring:
  cloud:
    consul:
      host: consul
      config:
        enabled: true
        format: YAML
        fail-fast: true
      discovery:
        enabled: true
        register: true
        fail-fast: true
```

**Accessing Configuration**:

```bash
# Get configuration from Consul KV store
curl http://localhost:8500/v1/kv/config/users-service,dev/data

# View all configuration keys
curl http://localhost:8500/v1/kv/?keys

# View Consul UI
open http://localhost:8500
```

**Profile Behavior**:

- **dev profile** (local development): Consul is disabled, services use local configuration files
- **dev-docker profile** (Docker Compose): Consul is enabled, services retrieve configuration from Consul KV store

### Adding a New Microservice

**Step 1: Create gRPC Interface**

```bash
# Create proto file in grpc-interfaces/<service-name>/
# Example: grpc-interfaces/orders/src/main/proto/orders.proto
```

**Step 2: Define Protocol Buffer Schema**

```protobuf
syntax = "proto3";
package br.com.jpbassinello.sbcgg.orders;

service OrdersService {
  rpc CreateOrder(CreateOrderRequest) returns (CreateOrderResponse);
}

message CreateOrderRequest {
  string customer_id = 1;
  repeated OrderItem items = 2;
}

message CreateOrderResponse {
  string order_id = 1;
}
```

**Step 3: Create Service Module**

```bash
mkdir -p services/grpc/orders/src/main/java/br/com/jpbassinello/sbcgg/orders
```

**Step 4: Implement Hexagonal Structure**

```
services/grpc/orders/
├── domain/
│   └── model/
│       └── Order.java
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   └── CreateOrderUseCase.java
│   │   └── out/
│   │       └── OrderRepository.java
│   └── service/
│       └── OrderService.java
└── adapter/
    ├── in/
    │   └── grpc/
    │       └── OrdersGrpcController.java
    └── out/
        └── persistence/
            └── OrderJpaAdapter.java
```

**Step 5: Add Dependencies**

```kotlin
// services/grpc/orders/build.gradle.kts
dependencies {
    api(project(":grpc-interfaces:orders"))
    api(project(":shared:grpc-server"))
    api(project(":shared:spring-jpa"))
}
```

**Step 6: Add Configuration**

```yaml
# services/grpc/orders/src/main/resources/application.yml
spring:
  application:
    name: orders-service
  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        enabled: true
        prefer-ip-address: true
grpc:
  server:
    port: 9093
```

**Step 7: Write Tests**

- Create unit tests for domain and application layers
- Create integration tests for adapters

### Adding a New Shared Module

```bash
# 1. Create directory structure
mkdir -p shared/new-module/src/{main,test}/java/br/com/jpbassinello/sbcgg/newmodule

# 2. Add to settings.gradle.kts
include(":shared:new-module")

# 3. Create build.gradle.kts
# shared/new-module/build.gradle.kts
dependencies {
    api("org.springframework.boot:spring-boot-starter")
}

# 4. Implement module
# 5. Write tests
# 6. Add to other modules' dependencies as needed
```

## Code Quality & Static Analysis

### Checkstyle

**Configuration:** `config/checkstyle/checkstyle.xml`
**Style Guide:** Google Java Style Guide

```bash
# Check main code
./gradlew checkstyleMain

# Check test code
./gradlew checkstyleTest

# Check all
./gradlew checkstyle
```

**Common Violations:**

- Line length > 100 characters
- Missing JavaDoc for public methods
- Incorrect import order
- Wildcard imports

### SpotBugs

**Status:** Currently disabled for Java 25 compatibility
**Re-enable:** Update `build.gradle.kts` when SpotBugs supports Java 25

```kotlin
// Uncomment in build.gradle.kts when Java 25 support is available
// apply(plugin = "com.github.spotbugs")
```

### JaCoCo Code Coverage

```bash
# Generate coverage report
./gradlew test jacocoTestReport

# View report
open build/reports/jacoco/test/html/index.html
```

**Coverage Goals:**

- **Unit Tests**: > 80% coverage
- **Integration Tests**: > 60% coverage
- **Overall**: > 70% coverage

### Code Quality Best Practices

✅ **Do:**

- Keep methods **short** (< 20 lines ideally)
- Use **meaningful names** for variables and methods
- Write **JavaDoc** for public APIs
- Follow **SOLID principles**
- Use **Lombok** to reduce boilerplate
- Use **MapStruct** for type-safe mapping
- Log at appropriate levels (TRACE, DEBUG, INFO, WARN, ERROR)

❌ **Don't:**

- Use magic numbers (define constants)
- Catch generic exceptions without handling
- Use `System.out.println` (use SLF4J)
- Ignore compiler warnings
- Commit commented-out code

## Architecture Patterns

### Hexagonal Architecture Rules

1. **Domain layer** has **no dependencies** on other layers
2. **Application layer** depends only on **domain**
3. **Adapters** depend on **application ports**
4. **Ports define interfaces**, adapters implement them
5. **Input ports** define use cases
6. **Output ports** define repository/external service contracts

### Dependency Rules

```
Adapter (In) → Port (In) → Service → Port (Out) ← Adapter (Out)
     ↓            ↓           ↓          ↓            ↓
  gRPC      UseCase     Implementation  Repository   JPA
```

### Communication Patterns

| Pattern     | Use Case                   | Example                   |
|-------------|----------------------------|---------------------------|
| **gRPC**    | Service-to-service sync    | Users → Messages          |
| **GraphQL** | Client-facing API          | Mobile/Web → Gateway      |
| **REST**    | Simple CRUD, external APIs | Admin endpoints           |
| **Events**  | Async communication        | Order created → Inventory |

## Common Tasks

### Managing Configuration (Consul)

**Adding Configuration for a New Service**:

1. Create a configuration file in `infrastructure/docker/compose/configs/consul/yaml/`

```yaml
# infrastructure/docker/compose/configs/consul/yaml/orders-service.yaml
spring:
  datasource:
    password: postgres

keycloak:
  password: keycloak
```

2. Update the init script to load the new configuration:

```bash
# Edit infrastructure/docker/compose/configs/consul/init.sh
consul kv put config/orders-service,dev/data @/tmp/yaml/orders-service.yaml
consul kv put config/orders-service,dev-docker/data @/tmp/yaml/orders-service.yaml
```

3. Restart Consul container to load the new configuration:

```bash
docker restart sbcgg-consul
```

**Updating Existing Configuration**:

1. Edit configuration files in `infrastructure/docker/compose/configs/consul/yaml/`
2. Update Consul KV store manually or restart Consul container:

```bash
# Option 1: Update manually
consul kv put config/users-service,dev/data @infrastructure/docker/compose/configs/consul/yaml/users-service.yaml

# Option 2: Restart Consul (reloads all configs via init script)
docker restart sbcgg-consul
```

3. Restart affected services to pick up new configuration

**Configuration Structure**:

Services use a layered configuration approach:

1. **Shared configuration**: `shared/spring-app/src/main/resources/config/app/` - Common settings for all services
2. **Service configuration**: `services/{service}/src/main/resources/config/` - Service-specific settings
3. **Consul KV store**: `infrastructure/docker/compose/configs/consul/yaml/` - Environment-specific secrets and
   overrides

**Testing Configuration**:

```bash
# View configuration from Consul KV store
curl http://localhost:8500/v1/kv/config/users-service,dev/data?raw

# View all configuration keys
curl http://localhost:8500/v1/kv/?keys

# Check if Consul config is loaded in a service
curl http://localhost:8090/actuator/env | jq '.propertySources[] | select(.name | contains("consul"))'
```

**Best Practices**:

- ✅ Store **sensitive data** in Consul KV store (passwords, tokens)
- ✅ Use **profiles** for different environments (dev, dev-docker, prod)
- ✅ Keep **non-sensitive defaults** in service configuration files
- ✅ Use local configuration files for the `dev` profile (no Consul dependency)
- ✅ Use Consul configuration for the `dev-docker` profile (Docker environment)
- ❌ Don't commit **production passwords** to Consul YAML files in version control
- ❌ Don't store **service logic** in configuration

### Updating gRPC Definitions

```bash
# 1. Modify .proto files in grpc-interfaces/<service>/src/main/proto/
# 2. Rebuild the interface module
./gradlew :grpc-interfaces:<service>:build

# 3. Rebuild services that depend on it
./gradlew :services:grpc:<service>:build
```

### Adding Database Migrations

```sql
-- Create migration file: shared/spring-jpa/src/main/resources/db/migration/V<version>__<description>.sql
-- Example: V001__create_users_table.sql

CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_username ON users (username);
```

### Configuring Redis Cache

```java

@Configuration
@EnableCaching
public class CacheConfig {

  @Bean
  public RedisCacheConfiguration cacheConfiguration() {
    return RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(10))
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer())
        );
  }
}
```

### Adding Distributed Locks (ShedLock)

```java

@Component
public class ScheduledTasks {

  @Scheduled(cron = "0 */15 * * * *")
  @SchedulerLock(
      name = "processOrders",
      lockAtMostFor = "10m",
      lockAtLeastFor = "5m"
  )
  public void processOrders() {
    // This will only run on one instance
  }
}
```

### Observability & Logging

**Grafana LGTM Stack**

The project uses Grafana's LGTM stack (Loki, Grafana, Tempo, Mimir) for comprehensive observability:

- **Loki**: Centralized log aggregation
- **Grafana**: Visualization and dashboards
- **Tempo**: Distributed tracing
- **Mimir**: Metrics storage

**Access**: http://localhost:3100

**OpenTelemetry Configuration**

Logs are automatically sent to the LGTM stack via the OpenTelemetry appender configured in
`shared/spring-app/src/main/resources/logback-spring.xml`:

```xml

<appender name="OpenTelemetry"
          class="io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender">
    <captureCodeAttributes>true</captureCodeAttributes>
    <captureMarkerAttribute>true</captureMarkerAttribute>
    <captureKeyValuePairAttributes>true</captureKeyValuePairAttributes>
    <captureLoggerContext>true</captureLoggerContext>
    <captureMdcAttributes>*</captureMdcAttributes>
</appender>
```

**Using Grafana**:

1. Open http://localhost:3100 in your browser
2. **Explore Logs**: Navigate to Explore → Select Loki as data source
3. **View Traces**: Navigate to Explore → Select Tempo as data source
4. **View Metrics**: Navigate to Explore → Select Mimir as data source
5. Logs are automatically correlated with traces via trace IDs

**Best Practices**:

- Use SLF4J for logging in your code
- Use MDC (Mapped Diagnostic Context) to add contextual information
- Use appropriate log levels (TRACE, DEBUG, INFO, WARN, ERROR)
- Include correlation IDs for request tracking
- Avoid logging sensitive information (passwords, tokens, PII)

## Troubleshooting

### Build Issues

**Problem:** `OutOfMemoryError` during build

```bash
# Solution: Increase Gradle heap size
export GRADLE_OPTS="-Xmx4g"
./gradlew build
```

**Problem:** Proto compilation fails

```bash
# Solution: Clean and rebuild proto modules
./gradlew :grpc-interfaces:clean :grpc-interfaces:build
```

### Runtime Issues

**Problem:** Service fails to start with Consul connection errors (dev-docker profile)

```bash
# Solution: Ensure Consul is running
docker ps | grep consul

# Check Consul health
curl http://localhost:8500/v1/status/leader

# Restart Consul if needed
docker restart sbcgg-consul

# Verify Consul is accessible
curl http://localhost:8500/v1/agent/self
```

**Problem:** Service not getting configuration from Consul (dev-docker profile)

```bash
# 1. Verify configuration exists in Consul KV store
curl http://localhost:8500/v1/kv/config/users-service,dev-docker/data?raw

# 2. Check if configuration was loaded during Consul startup
docker logs sbcgg-consul | grep "Initializing KV pairs"

# 3. Manually load configuration if needed
docker exec -it sbcgg-consul consul kv put config/users-service,dev-docker/data @/tmp/yaml/users-service.yaml

# 4. Check service application profile is set correctly
# Services must use dev-docker profile to enable Consul
SPRING_PROFILES_ACTIVE=dev-docker ./gradlew :services:grpc:users:bootRun

# 5. Verify Consul config is enabled in shared/spring-app/src/main/resources/config/app/application-dev-docker.yaml
spring:
  cloud:
    consul:
      config:
        enabled: true
```

**Problem:** Service not registering with Consul (dev-docker profile)

```bash
# Note: Service discovery is only enabled in dev-docker profile
# Check if Consul is running
docker ps | grep consul

# Check Consul health
curl http://localhost:8500/v1/status/leader

# View registered services in Consul
curl http://localhost:8500/v1/catalog/services

# Check Consul UI for service health
# Open http://localhost:8500 in browser

# Verify Consul discovery is enabled in shared/spring-app/src/main/resources/config/app/application-dev-docker.yaml
spring:
  cloud:
    consul:
      host: consul
      discovery:
        enabled: true
        register: true
        fail-fast: true

# Check service logs for registration errors
# Look for "Registering service with consul" or similar messages

# For local development (dev profile):
# Consul discovery is disabled by default. Services communicate via localhost.
```

**Problem:** Consul container not starting

```bash
# Check Consul logs
docker logs sbcgg-consul

# Restart Consul container
docker restart sbcgg-consul

# Verify Consul is accessible
curl http://localhost:8500/v1/agent/self
```

**Problem:** Database connection fails

```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Check logs
docker logs sbcgg-postgres
```

**Problem:** Redis connection fails

```bash
# Verify Redis is accessible
redis-cli -a redis ping

# Check Docker container is running
docker ps | grep redis

# Check application.yml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: redis
```

**Problem:** Grafana/LGTM not accessible

```bash
# Check if LGTM container is running
docker ps | grep lgtm

# Check logs for errors
docker logs sbcgg-lgtm

# Restart container if needed
docker restart sbcgg-lgtm

# Verify health check
curl http://localhost:3100
```

**Problem:** Logs not appearing in Grafana/Loki

```bash
# 1. Verify OpenTelemetry appender is configured in logback-spring.xml
# 2. Check that 'dev' profile is active (OpenTelemetry only enabled in dev profile)
# 3. Verify OTLP endpoint is accessible:
curl http://localhost:4318/v1/logs

# 4. Check application logs for OpenTelemetry errors
# 5. In Grafana, check Loki data source configuration
```

### Test Issues

**Problem:** Integration tests fail with "Cannot load context"

```java
// Solution: Add @ActiveProfiles("test")
@SpringBootTest
@ActiveProfiles("test")
class MyServiceIT {}
```

**Problem:** Tests pass locally but fail in CI

```bash
# Solution: Ensure test isolation
# - Don't share state between tests
# - Use @DirtiesContext if needed
# - Check for timing issues
```

## Performance Optimization

### Gradle Build Cache

The project is configured for optimal build performance:

```properties
# gradle.properties
org.gradle.caching=true          # Enable build cache
org.gradle.parallel=true         # Parallel execution
org.gradle.configureondemand=true # On-demand configuration
```

**Verify cache effectiveness:**

```bash
./gradlew clean build --scan
# Opens build scan in browser showing cache hits
```

### Development Tips

- Use **Gradle daemon** (enabled by default)
- Use **incremental compilation**
- Use **test filters** when iterating on specific tests
- Use **IDE integration** for faster feedback

```bash
# Run only changed tests
./gradlew test --rerun-tasks

# Continuous build
./gradlew -t test
```

## Additional Resources

- [README.md](README.md) - Public project documentation
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/)
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Spring Cloud Consul Documentation](https://docs.spring.io/spring-cloud-consul/reference/)
- [Spring gRPC Documentation](https://docs.spring.io/spring-grpc/reference/)
- [Consul Documentation](https://developer.hashicorp.com/consul/docs)
- [gRPC Java Documentation](https://grpc.io/docs/languages/java/)
- [GraphQL Java Documentation](https://www.graphql-java.com/documentation/getting-started)
- [MapStruct Documentation](https://mapstruct.org/documentation/stable/reference/html/)

---

**For questions or clarifications**, please refer to the team leads or open a discussion in the project repository.

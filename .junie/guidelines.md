# SBCGG Development Guidelines

For general project information, see [README.md](README.md).
Detailed guides are in subdirectory `CLAUDE.md` files — they load automatically when working in those directories.

## Quick Reference

### Essential Commands

```bash
./gradlew build                          # Build entire project
./gradlew test                           # Run all tests
./gradlew :services:grpc:users:bootRun   # Start a gRPC service
./gradlew :services:graphql:gateway:bootRun  # Start GraphQL gateway
./gradlew jacocoTestReport               # Code coverage report
./gradlew checkstyleMain checkstyleTest  # Check code style
```

### Key URLs (Local Development)

- **Consul Dashboard**: http://localhost:8500
- **Keycloak Admin**: http://localhost:9090
- **Grafana Dashboard**: http://localhost:3100 (LGTM: Loki, Grafana, Tempo, Mimir)
- **GraphQL Playground**: http://localhost:8080/graphiql
- **Users Service**: http://localhost:8090 (gRPC: 8091)

## Prerequisites

- **Java 25 (LTS)** - Required
- **Gradle 9.2.1** - Wrapper included
- **Docker & Docker Compose** - For infrastructure services

## Project Structure

```
sbcgg/
├── grpc-interfaces/          # Protocol Buffer definitions
│   ├── messages/            # Message service protos
│   └── users/               # User service protos
├── services/                 # Microservice implementations
│   ├── graphql/gateway/     # GraphQL API gateway
│   └── grpc/                # gRPC microservices
│       ├── messages/        # Messages service
│       └── users/           # Users service
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
└── tests/                    # Test modules
    └── e2e/                 # End-to-end tests
```

## Architecture Rules

### Hexagonal Architecture (Per Service)

```
service/
├── domain/                   # Domain models & business logic (NO external dependencies)
│   └── model/
├── application/             # Application layer (depends only on domain)
│   ├── port/
│   │   ├── in/             # Input ports (use case interfaces)
│   │   └── out/            # Output ports (repository interfaces)
│   └── service/            # Use case implementations
└── adapter/                 # Adapters (depend on application ports)
    ├── in/                  # Input adapters (gRPC, REST)
    └── out/                 # Output adapters (persistence, external)
```

**Dependency flow:**
```
Adapter (In) → Port (In) → Service → Port (Out) ← Adapter (Out)
```

### Communication Patterns

| Pattern     | Use Case                   | Example                   |
|-------------|----------------------------|---------------------------|
| **gRPC**    | Service-to-service sync    | Users → Messages          |
| **GraphQL** | Client-facing API          | Mobile/Web → Gateway      |
| **REST**    | Simple CRUD, external APIs | Admin endpoints           |
| **Events**  | Async communication        | Order created → Inventory |

### Key Design Patterns

- **Hexagonal Architecture**: Clear separation of concerns
- **Domain-Driven Design (DDD)**: Domain models at the core
- **Dependency Inversion**: Domain layer has no dependencies
- **Repository Pattern**: Data access abstraction
- **Use Case Pattern**: Application services as use cases

## Dependency Management

Uses **Gradle Version Catalogs** (defined in `settings.gradle.kts`). Key dependencies:

| Category      | Library         | Purpose                       |
|---------------|-----------------|-------------------------------|
| Framework     | Spring Boot 4.0.3 | Application framework         |
| Cloud         | Spring Cloud    | Microservices toolkit         |
| Discovery     | Consul          | Service discovery & config    |
| Security      | Keycloak        | Identity & access management  |
| RPC           | Spring gRPC     | High-performance RPC          |
| API           | GraphQL Java    | GraphQL implementation        |
| Database      | PostgreSQL      | Relational database           |
| Cache         | Redis           | Distributed cache             |
| Mapping       | MapStruct       | Bean mapping                  |
| Observability | OpenTelemetry   | Distributed tracing & logging |

## Code Quality Rules

**Checkstyle**: Google Java Style Guide (`config/checkstyle/checkstyle.xml`)
**Coverage goals**: Unit > 80%, Integration > 60%, Overall > 70%

**Do:**
- Keep methods short (< 20 lines)
- Use meaningful names
- Write JavaDoc for public APIs
- Follow SOLID principles
- Use Lombok to reduce boilerplate
- Use MapStruct for type-safe mapping
- Log at appropriate levels (SLF4J)

**Don't:**
- Use magic numbers (define constants)
- Catch generic exceptions without handling
- Use `System.out.println` (use SLF4J)
- Ignore compiler warnings
- Commit commented-out code
- Use wildcard imports

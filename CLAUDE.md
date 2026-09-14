# SBCGG Development Guidelines

For general project information, see [README.md](README.md).
Detailed guides are in subdirectory `CLAUDE.md` files — they load automatically when working in those directories. Cross-cutting rules live in `.claude/rules/`, and step-by-step procedures are slash-command skills under `.claude/skills/`; both are indexed at the bottom of this file.

## Quick Reference

### Essential Commands

```bash
./gradlew build                          # Build entire project
./gradlew test                           # Run all tests
./gradlew :services:grpc:users:bootRun   # Start a gRPC service
./gradlew :services:graphql:gateway:bootRun  # Start GraphQL gateway
./gradlew jacocoTestReport               # Code coverage report
./gradlew checkstyleMain checkstyleTest  # Check code style
./gradlew spotbugsMain                   # Static analysis
```

```bash
cd ui && pnpm install && pnpm codegen    # Frontend: install + generate GraphQL types
cd ui && pnpm dev                        # Frontend dev server
```

### Key URLs (Local Development)

- **Consul Dashboard**: http://localhost:8500
- **Keycloak Admin**: http://localhost:9090
- **Grafana Dashboard**: http://localhost:3100 (LGTM: Loki, Grafana, Tempo, Mimir)
- **GraphQL Playground**: http://localhost:8080/graphiql
- **Users Service**: http://localhost:8090 (gRPC: 8091)
- **Messages Service**: http://localhost:8190 (gRPC: 8191)
- **MinIO Console**: http://localhost:9001 (S3 API on 9000)
- **UI (Next.js)**: http://localhost:3000

## Prerequisites

- **Java 25 (LTS)** - Required
- **Gradle 9.7.1** - Wrapper included
- **Docker & Docker Compose** - For infrastructure services
- **Node 22+ and pnpm** - For the `ui/` frontend only

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
│   ├── ai-client/           # LLM port + OpenAI adapter
│   ├── exception/           # Exception handling
│   ├── grpc-client/         # gRPC client utilities
│   ├── grpc-server/         # gRPC server utilities
│   ├── mapping/             # Object mapping
│   ├── proto-mapping/       # Protobuf mapping
│   ├── spring-app/          # Spring base config
│   ├── spring-cache/        # Cache config (Redis)
│   ├── spring-jpa/          # JPA/database config
│   ├── spring-ratelimit/    # Redis-backed rate limiting (@RateLimiter)
│   ├── spring-shedlock/     # Distributed locking
│   ├── spring-storage/      # S3-compatible object storage
│   ├── util/                # General utilities
│   └── validation/          # Validation utilities
├── infrastructure/           # Deployment
│   ├── docker/compose/      # dev + shared + prod (Traefik) stacks
│   └── kubernetes/          # Kustomize manifests
├── tests/                    # Test modules
│   └── e2e/                 # End-to-end tests
└── ui/                       # Next.js frontend (pnpm)
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

## Deployment

Two paths, both in `infrastructure/`:

- **Docker Compose on a single host** — `infrastructure/docker/compose/prod/` (Traefik terminating
  TLS, named volumes, Consul seeded by `init-prod.sh`). Driven by
  `.github/workflows/publish.yml`: build → push per-commit images to GHCR → SSH deploy → health
  and version-skew verification. `prod/docker-compose.yml` is standalone (it does **not**
  `include:` `shared/`), so image tags must be bumped there too.
- **Kubernetes** — `infrastructure/kubernetes/` (Kustomize).

`prod/.env.example` documents every secret the prod stack needs; the real `.env` lives only on the
deploy host and is gitignored.

## Dependency Management

Uses **Gradle Version Catalogs** (defined in `settings.gradle.kts`). Key dependencies:

| Category      | Library         | Purpose                       |
|---------------|-----------------|-------------------------------|
| Framework     | Spring Boot 4.1.1 | Application framework         |
| Cloud         | Spring Cloud    | Microservices toolkit         |
| Discovery     | Consul          | Service discovery & config    |
| Security      | Keycloak        | Identity & access management  |
| RPC           | Spring gRPC     | High-performance RPC          |
| API           | GraphQL Java    | GraphQL implementation        |
| Database      | PostgreSQL      | Relational database           |
| Cache         | Redis           | Distributed cache             |
| Mapping       | MapStruct       | Bean mapping                  |
| Observability | OpenTelemetry   | Distributed tracing & logging |
| Storage       | AWS SDK (S3)    | Object storage (MinIO in dev) |
| Frontend      | Next.js + urql  | Client app (`ui/`, pnpm)      |

Several catalog entries carry comments explaining why they are pinned where they are
(OpenTelemetry ↔ Boot's OTel core, protoc ↔ resolved protobuf/grpc, the SpotBugs hold). Read them
before bumping — each records a build or startup failure. `/upgrade-deps` lists the known
breaking changes to check for.

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

## Detailed Guidelines

Rules (`.claude/rules/`):

- **MapStruct**: `mapstruct.md` — proto ↔ domain mapping via the entur protobuf SPI; don't
  hand-write enum switches or presence checks
- **Troubleshooting**: `troubleshooting.md` — Consul, Postgres, Redis, LGTM, test and build issues

Skills (invoke with `/<name>`):

- `/new-microservice` — add a gRPC service module
- `/new-shared-module` — add a shared library module
- `/consul-config` — manage Consul KV configuration
- `/database-migration` — migrations, Redis cache, ShedLock
- `/fix-spotbugs` — run and fix static analysis
- `/upgrade-deps` — upgrade Gradle/Docker versions and docs
- `/upgrade-ui-dependencies` — upgrade the `ui/` frontend via pnpm

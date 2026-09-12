# Infrastructure Guide

## Docker Compose Setup

```bash
cd infrastructure/docker/compose/dev
docker-compose up -d
cd -
```

### Services Started

| Service      | Port(s)    | Container Name   |
|--------------|------------|------------------|
| PostgreSQL   | 5432       | sbcgg-postgres   |
| Redis        | 6379       | sbcgg-redis      |
| Consul       | 8500       | sbcgg-consul     |
| Keycloak     | 9090       | sbcgg-keycloak   |
| Grafana LGTM | 3100, 4318 | sbcgg-lgtm       |
| MinIO        | 9000, 9001 | sbcgg-minio      |

Note the layering: `dev/docker-compose.yml` `include:`s `shared/docker-compose.yml`, which holds
everything that is not dev-only (Postgres, Keycloak, Redis, MinIO). Consul, LGTM and the app
containers live in `dev/`.

Postgres port 5432 is published as 5433 on the host to avoid colliding with a local install.

## Production Stack

```bash
cd infrastructure/docker/compose/prod
cp .env.example .env      # fill in every value; .env is gitignored
docker compose up -d
```

Traefik fronts the stack, terminates TLS (Let's Encrypt HTTP-01) and routes by hostname; only
80/443 are published. Consul is seeded by `configs/consul/init-prod.sh`, which **skips keys that
already exist** so a hand-edited KV value survives a redeploy.

`prod/docker-compose.yml` is **standalone** — it does not `include:` `shared/`, because the
infrastructure containers need production settings (real passwords, tuned Postgres, named volumes,
no published host ports). The cost is duplicated image tags: bumping a version in `shared/` or
`dev/` without bumping `prod/` is silent version drift in production.

Deploys run through `.github/workflows/publish.yml` (build → push per-commit images to GHCR → SSH
deploy → health check → version-skew check). The alternative deployment target, Kubernetes
manifests, lives in `infrastructure/kubernetes/`.

## Consul KV Seeds

`configs/consul/yaml/` holds one file per service plus a shared `application.yaml`. Values there
are placeholder **expressions** (`${POSTGRES_PASSWORD:postgres}`), never literal secrets: the
default after the colon keeps the local stack working while the container's environment supplies
the real value in `prod-docker`. A literal committed here would become the permanent production
value on first boot, because `init-prod.sh` never overwrites.

## Observability (Grafana LGTM Stack)

- **Loki**: Centralized log aggregation
- **Grafana**: Visualization and dashboards (http://localhost:3100)
- **Tempo**: Distributed tracing
- **Mimir**: Metrics storage

### OpenTelemetry Configuration

Logs are sent to LGTM via the OpenTelemetry appender in `shared/spring-app/src/main/resources/logback-spring.xml`:

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

### Using Grafana

1. Open http://localhost:3100
2. **Logs**: Explore -> Loki data source
3. **Traces**: Explore -> Tempo data source
4. **Metrics**: Explore -> Mimir data source
5. Logs are automatically correlated with traces via trace IDs

### Logging Best Practices

- Use SLF4J for logging
- Use MDC for contextual information
- Use appropriate log levels (TRACE, DEBUG, INFO, WARN, ERROR)
- Include correlation IDs for request tracking
- Avoid logging sensitive information (passwords, tokens, PII)

## Performance Optimization

### Gradle Build Cache

```properties
# gradle.properties
org.gradle.caching=true
org.gradle.parallel=true
org.gradle.configureondemand=true
```

```bash
./gradlew clean build --scan  # Verify cache effectiveness
```

## Troubleshooting

### Build Issues

**`OutOfMemoryError` during build:**
```bash
export GRADLE_OPTS="-Xmx4g"
./gradlew build
```

**Proto compilation fails:**
```bash
./gradlew :grpc-interfaces:clean :grpc-interfaces:build
```

### Consul Issues

**Service fails to start with Consul connection errors (dev-docker profile):**
```bash
docker ps | grep consul
curl http://localhost:8500/v1/status/leader
docker restart sbcgg-consul
```

**Service not getting configuration from Consul (dev-docker profile):**
```bash
curl http://localhost:8500/v1/kv/config/users-service,dev-docker/data?raw
docker logs sbcgg-consul | grep "Initializing KV pairs"
# Manually load if needed:
docker exec -it sbcgg-consul consul kv put config/users-service,dev-docker/data @/tmp/yaml/users-service.yaml
# Ensure correct profile:
SPRING_PROFILES_ACTIVE=dev-docker ./gradlew :services:grpc:users:bootRun
```

**Service not registering with Consul (dev-docker profile):**
```bash
curl http://localhost:8500/v1/catalog/services
# Service discovery is only enabled in dev-docker profile
# For local dev (dev profile): Consul discovery is disabled, services use localhost
```

**Consul container not starting:**
```bash
docker logs sbcgg-consul
docker restart sbcgg-consul
curl http://localhost:8500/v1/agent/self
```

### Database Issues

**Database connection fails:**
```bash
docker ps | grep postgres
docker logs sbcgg-postgres
```

### Redis Issues

**Redis connection fails:**
```bash
redis-cli -a redis ping
docker ps | grep redis
```

### Grafana/LGTM Issues

**Not accessible:**
```bash
docker ps | grep lgtm
docker logs sbcgg-lgtm
docker restart sbcgg-lgtm
curl http://localhost:3100
```

**Logs not appearing in Grafana/Loki:**
1. Verify OpenTelemetry appender in logback-spring.xml
2. Check `dev` profile is active (OpenTelemetry only enabled in dev profile)
3. Verify OTLP endpoint: `curl http://localhost:4318/v1/logs`
4. Check application logs for OpenTelemetry errors

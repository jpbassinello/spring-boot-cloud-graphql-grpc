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

# Troubleshooting

## Build Issues

**Problem:** `OutOfMemoryError` during build

```bash
export GRADLE_OPTS="-Xmx4g"
./gradlew build
```

**Problem:** Proto compilation fails

```bash
./gradlew :grpc-interfaces:clean :grpc-interfaces:build
```

## Runtime Issues

**Problem:** Service fails to start with Consul connection errors (dev-docker profile)

```bash
docker ps | grep consul
curl http://localhost:8500/v1/status/leader
docker restart sbcgg-consul
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
SPRING_PROFILES_ACTIVE=dev-docker ./gradlew :services:grpc:users:bootRun

# 5. Verify Consul config is enabled in shared/spring-app/src/main/resources/config/app/application-dev-docker.yaml
```

**Problem:** Service not registering with Consul (dev-docker profile)

```bash
# Service discovery is only enabled in dev-docker profile
docker ps | grep consul
curl http://localhost:8500/v1/status/leader
curl http://localhost:8500/v1/catalog/services
# Check Consul UI: http://localhost:8500

# For local development (dev profile):
# Consul discovery is disabled by default. Services communicate via localhost.
```

**Problem:** Consul container not starting

```bash
docker logs sbcgg-consul
docker restart sbcgg-consul
curl http://localhost:8500/v1/agent/self
```

**Problem:** Database connection fails

```bash
docker ps | grep postgres
docker logs sbcgg-postgres
```

**Problem:** Redis connection fails

```bash
redis-cli -a redis ping
docker ps | grep redis
```

**Problem:** Grafana/LGTM not accessible

```bash
docker ps | grep lgtm
docker logs sbcgg-lgtm
docker restart sbcgg-lgtm
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

## Test Issues

**Problem:** Integration tests fail with "Cannot load context"

```java
// Solution: Add @ActiveProfiles("test")
@SpringBootTest
@ActiveProfiles("test")
class MyServiceIT {}
```

**Problem:** Tests pass locally but fail in CI

```bash
# Ensure test isolation:
# - Don't share state between tests
# - Use @DirtiesContext if needed
# - Check for timing issues
```

## Performance Optimization

### Gradle Build Cache

```properties
# gradle.properties (already configured)
org.gradle.caching=true
org.gradle.parallel=true
org.gradle.configureondemand=true
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

# Verify cache effectiveness
./gradlew clean build --scan
```
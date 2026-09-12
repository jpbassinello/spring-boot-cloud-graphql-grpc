---
name: database-migration
description: Guide for adding database migrations, configuring Redis cache, and setting up distributed locks with ShedLock.
user-invocable: true
---

# Database & Infrastructure Tasks

## Adding Database Migrations

Create migration files in `shared/spring-jpa/src/main/resources/db/migration/`:

```sql
-- V<version>__<description>.sql
-- Example: V001__create_users_table.sql

CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_username ON users (username);
```

## Configuring Redis Cache

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

## Adding Distributed Locks (ShedLock)

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

## Observability & Logging

### Grafana LGTM Stack

The project uses Grafana's LGTM stack (Loki, Grafana, Tempo, Mimir):

- **Loki**: Centralized log aggregation
- **Grafana**: Visualization and dashboards (http://localhost:3100)
- **Tempo**: Distributed tracing
- **Mimir**: Metrics storage

### OpenTelemetry

Logs are sent to LGTM via the OpenTelemetry appender in `shared/spring-app/src/main/resources/logback-spring.xml`.

### Using Grafana

1. Open http://localhost:3100
2. **Explore Logs**: Navigate to Explore -> Select Loki
3. **View Traces**: Navigate to Explore -> Select Tempo
4. **View Metrics**: Navigate to Explore -> Select Mimir

### Logging Best Practices

- Use SLF4J for logging
- Use MDC for contextual information
- Use appropriate log levels (TRACE, DEBUG, INFO, WARN, ERROR)
- Include correlation IDs for request tracking
- Avoid logging sensitive information (passwords, tokens, PII)

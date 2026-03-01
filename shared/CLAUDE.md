# Shared Modules Guide

## Adding a New Shared Module

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

## Database Migrations

Migration files go in `shared/spring-jpa/src/main/resources/db/migration/`:

```sql
-- V<version>__<description>.sql (e.g., V001__create_users_table.sql)
CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_username ON users (username);
```

## Redis Cache Configuration

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

## Distributed Locks (ShedLock)

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

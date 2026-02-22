# Sharing Testcontainers Configuration with Gradle Test Fixtures

This is the third post in the SBCGG series. If you haven't read the previous ones, check them out: [Introducing SBCGG](1-About-SBCGG.md) and [gRPC in SBCGG](2-gRPC-in-SBCGG.md).

**GitHub**: https://github.com/jpbassinello/spring-boot-cloud-graphql-grpc

---

## The Problem: Duplicated Test Infrastructure

As SBCGG grew from one service to two, I noticed a pattern that every backend developer working with microservices will recognize: **duplicated test infrastructure code**.

Both the Users and Messages services use PostgreSQL and Testcontainers for integration tests. Each service had its own `PostgresContainer` interface, and they were nearly identical:

```java
// services/grpc/users/src/test/java/.../PostgresContainer.java
public interface PostgresContainer {

  @Container
  @ServiceConnection
  PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18.2-alpine3.23")
      .withDatabaseName("users")
      .withUsername("postgres")
      .withPassword("postgres");
}

// services/grpc/messages/src/test/java/.../PostgresContainer.java
interface PostgresContainer {

  @Container
  @ServiceConnection
  PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18.2-alpine3.23")
      .withDatabaseName("messages")
      .withUsername("postgres")
      .withPassword("postgres");
}
```

Same image, same credentials, same pattern. The only difference was the database name, which doesn't even matter because each test suite spins up its own isolated container. This is the kind of duplication that becomes a maintenance burden as you add more services. Imagine updating the PostgreSQL image version across five or ten services.

## The Solution: Gradle Test Fixtures

Gradle has a feature called **test fixtures** that is specifically designed for this kind of problem. Test fixtures allow a module to export test utilities, shared configurations, and helper classes that other modules can use in their tests.

The idea is simple: instead of each service defining its own `PostgresContainer`, we define it once in the `shared:spring-jpa` module (since all services that need PostgreSQL already depend on it) and expose it as a test fixture.

### Step 1: Enable the Plugin and Add Dependencies

In the `shared:spring-jpa` module, we apply the `java-test-fixtures` plugin and declare the Testcontainers dependencies under the `testFixturesApi` configuration:

```kotlin
// shared/spring-jpa/build.gradle.kts
plugins {
    `java-test-fixtures`
}

dependencies {
    // ... production dependencies ...

    testFixturesApi(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    testFixturesApi(platform("org.testcontainers:testcontainers-bom:${rootProject.libs.versions.testcontainers.get()}"))
    testFixturesApi("org.springframework.boot:spring-boot-testcontainers")
    testFixturesApi("org.testcontainers:testcontainers-junit-jupiter")
    testFixturesApi("org.testcontainers:testcontainers-postgresql")
}
```

One important detail: the `testFixturesApi` configuration does **not** inherit from `implementation`, so it doesn't automatically get the BOMs (Bill of Materials) declared in the root project. That's why we explicitly add the Spring Boot and Testcontainers platform BOMs here. Without them, Gradle can't resolve the dependency versions.

### Step 2: Create the Shared Container

Test fixtures live under `src/testFixtures` in the module directory. The structure mirrors the standard source set:

```
shared/spring-jpa/
├── src/
│   ├── main/java/...          # Production code
│   └── testFixtures/java/...  # Test fixtures (shared test utilities)
```

The shared `PostgresContainer` interface:

```java
// shared/spring-jpa/src/testFixtures/java/br/com/jpbassinello/sbcgg/jpa/test/PostgresContainer.java
package br.com.jpbassinello.sbcgg.jpa.test;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;

public interface PostgresContainer {

  @Container
  @ServiceConnection
  PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18.2-alpine3.23")
      .withDatabaseName("test")
      .withUsername("postgres")
      .withPassword("postgres");
}
```

### Step 3: Consume from Services

In the services' parent build file, we replace the three individual Testcontainers dependencies with a single test fixture dependency:

```kotlin
// services/grpc/build.gradle.kts (before)
subprojects {
    dependencies {
        // ...
        testImplementation("org.springframework.boot:spring-boot-testcontainers")
        testImplementation("org.testcontainers:testcontainers-junit-jupiter")
        testImplementation("org.testcontainers:testcontainers-postgresql")
    }
}

// services/grpc/build.gradle.kts (after)
subprojects {
    dependencies {
        // ...
        testImplementation(testFixtures(project(":shared:spring-jpa")))
    }
}
```

The `testFixtures(project(...))` function tells Gradle to depend on the test fixtures of the specified module, not its production code (which is already a transitive dependency). This single line brings in the `PostgresContainer` class and all the Testcontainers dependencies it needs.

### Step 4: Update Imports

The only change in the integration tests is the import statement:

```java
// Before
import br.com.jpbassinello.sbcgg.services.grpc.users.adapter.PostgresContainer;

// After
import br.com.jpbassinello.sbcgg.jpa.test.PostgresContainer;
```

The `@ImportTestcontainers(PostgresContainer.class)` annotation stays exactly the same. Tests continue to work as before, with zero changes to test logic.

## How This Compares to Maven

Maven doesn't have a built-in equivalent to Gradle's test fixtures. If you're working with Maven, the typical approach is to create a **separate module** that packages your shared test utilities as a regular JAR:

```xml
<!-- shared-test-support/pom.xml -->
<project>
    <artifactId>shared-test-support</artifactId>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-testcontainers</artifactId>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>postgresql</artifactId>
        </dependency>
    </dependencies>
</project>
```

Then each service declares it as a test-scoped dependency:

```xml
<!-- services/users/pom.xml -->
<dependency>
    <groupId>br.com.jpbassinello</groupId>
    <artifactId>shared-test-support</artifactId>
    <scope>test</scope>
</dependency>
```

This works, but it has drawbacks:

| Aspect | Gradle Test Fixtures | Maven Separate Module |
|---|---|---|
| **Module count** | Same module (`shared:spring-jpa`) | Requires a new module |
| **Coupling** | Test utilities live next to the code they support | Test utilities are decoupled from the module they're related to |
| **Dependency scope** | Gradle automatically scopes fixtures to test classpath only | You must manually manage `<scope>test</scope>` |
| **Configuration** | Single plugin + `testFixturesApi` | Full `pom.xml` with its own dependency management |
| **Discoverability** | Fixtures are in `src/testFixtures` alongside `src/main` and `src/test` | Fixtures are in a completely separate directory |

The key advantage of Gradle's approach is **co-location**. The test fixtures for the JPA module live inside the JPA module itself. This makes it obvious where to find and maintain them. With Maven, you end up with an extra module that exists purely for test infrastructure, which adds overhead to the project structure and build lifecycle.

Maven also has a `maven-jar-plugin` option to generate a test JAR (`<classifier>tests</classifier>`), but this is generally discouraged because it exports **all** test classes from a module, not just the ones you intend to share.

## The Result

After this refactoring:

- **One `PostgresContainer` definition** instead of one per service
- **One place to update** the PostgreSQL image version
- **Fewer Testcontainers dependencies** scattered across build files
- **Adding a new service** with PostgreSQL integration tests requires zero boilerplate for container setup

This is a small change, but it's the kind of infrastructure hygiene that compounds over time. When the project grows to five, ten, or more services, having a single source of truth for test infrastructure pays off.

---

## What's Next?

In the next posts, I plan to cover:

- **How Spring Boot 4 simplified observability** and why metrics and traces are essential in microservices
- **Hexagonal Architecture in production microservices projects**

---

I'd love to hear your feedback. If you think this could help other developers, please share it.

**GitHub**: https://github.com/jpbassinello/spring-boot-cloud-graphql-grpc

Thank you for reading.

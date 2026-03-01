# Testing Guide

## Test Structure

| Type              | Pattern      | Purpose                     | Speed  | Dependencies             |
|-------------------|--------------|-----------------------------|--------|--------------------------|
| Unit Tests        | `*Test.java` | Test individual components  | Fast   | None (mocked)            |
| Integration Tests | `*IT.java`   | Test with real dependencies | Slower | Spring context, DB, etc. |

## Running Tests

```bash
./gradlew test                           # All tests
./gradlew :shared:util:test              # Specific module
./gradlew :shared:util:test --tests "br.com.jpbassinello.sbcgg.utils.DateTimeUtilsTest"  # Specific class
./gradlew :shared:util:test --tests "br.com.jpbassinello.sbcgg.utils.DateTimeUtilsTest.isFutureDate"  # Specific method
./gradlew test jacocoTestReport          # With coverage report
```

## Unit Test Template

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

## Integration Test Template

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

## Best Practices

**Do:**
- Use **Arrange-Act-Assert** pattern consistently
- Write **descriptive test names** that explain the scenario
- Use `@DisplayName` for complex test descriptions
- Mock **external dependencies** in integration tests
- Use `@DataJpaTest` for repository tests
- Use `@ActiveProfiles("test")` for test-specific configuration
- Test **edge cases** and **error conditions**
- Keep tests **independent** and **idempotent**
- Prefer full object asserting with `usingRecursiveComparison()`

**Don't:**
- Share state between tests
- Use hard-coded sleep/wait statements
- Test implementation details
- Create tests that depend on execution order
- Mix unit and integration test concerns

## Troubleshooting

**Integration tests fail with "Cannot load context":**
```java
@SpringBootTest
@ActiveProfiles("test")
class MyServiceIT {}
```

**Tests pass locally but fail in CI:**
- Don't share state between tests
- Use `@DirtiesContext` if needed
- Check for timing issues

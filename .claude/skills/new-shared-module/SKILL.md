---
name: new-shared-module
description: Step-by-step guide for adding a new shared module to the project.
user-invocable: true
---

# Adding a New Shared Module

## Step 1: Create Directory Structure

```bash
mkdir -p shared/new-module/src/{main,test}/java/br/com/jpbassinello/sbcgg/newmodule
```

## Step 2: Add to settings.gradle.kts

```kotlin
include(":shared:new-module")
```

## Step 3: Create build.gradle.kts

```kotlin
// shared/new-module/build.gradle.kts
dependencies {
    api("org.springframework.boot:spring-boot-starter")
}
```

## Step 4: Implement Module

Follow the project conventions:
- Use `br.com.jpbassinello.sbcgg.<modulename>` package
- Follow SOLID principles
- Use Lombok to reduce boilerplate

## Step 5: Write Tests

- Unit tests in `src/test/java/`
- See `.claude/rules/testing.md` for test templates

## Step 6: Add to Other Modules

Add as dependency in consuming modules:

```kotlin
// In other module's build.gradle.kts
dependencies {
    api(project(":shared:new-module"))
}
```

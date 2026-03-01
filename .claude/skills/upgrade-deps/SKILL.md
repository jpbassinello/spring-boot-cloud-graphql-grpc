---
name: upgrade-deps
description: Upgrade project dependencies, Docker images, and documentation versions. Searches for latest stable versions of Gradle/Maven dependencies, Docker Hub images, and updates all config files and docs. Use when the user asks to upgrade dependencies, bump versions, or update the project's tech stack.
disable-model-invocation: true
argument-hint: "[all|java|docker|kubernetes|docs]"
---

# Dependency Upgrade Skill

Upgrade all project dependencies, Docker images, and documentation to their latest stable versions.

**Scope**: `$ARGUMENTS` (defaults to `all` if empty)

Supported scopes: `all`, `java`, `docker`, `kubernetes`, `docs`

---

## Phase 1: Catalog Current Versions

Read current versions from all sources before making any changes.

### Java Dependencies (`settings.gradle.kts`)

Read the version catalog block in `settings.gradle.kts`. Record every `version(...)` and `plugin(...)` entry:

| Key | Maven Coordinates |
|-----|-------------------|
| `spring-security` | `org.springframework.security:spring-security-bom` |
| `spring-cloud` | `org.springframework.cloud:spring-cloud-dependencies` |
| `opentelemetry` | `io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom` |
| `spring-grpc` | `org.springframework.grpc:spring-grpc-dependencies` |
| `protoc-protobuf` | `com.google.protobuf:protobuf-java` (PINNED by spring-grpc BOM) |
| `protoc-grpc` | `io.grpc:grpc-bom` (PINNED by spring-grpc BOM) |
| `keycloak` | `org.keycloak:keycloak-admin-client` |
| `mapstruct` | `org.mapstruct:mapstruct` |
| `spi-protobuf-mapstruct` | `no.entur.mapstruct.spi:protobuf-spi-impl` |
| `shedlock` | `net.javacrumbs.shedlock:shedlock-core` |
| `testcontainers` | `org.testcontainers:testcontainers-bom` |

Plugins:

| Key | Plugin ID |
|-----|-----------|
| `spring-boot` | `org.springframework.boot` |
| `spotbugs` | `com.github.spotbugs` |
| `protobuf` | `com.google.protobuf` |
| `buf` | `build.buf` |
| `lombok` | `io.freefair.lombok` |

### Hardcoded Versions (`build.gradle.kts`)

| Artifact | What to grep |
|----------|-------------|
| `com.github.spotbugs:spotbugs-annotations` | `spotbugs-annotations:` |
| `org.junit.platform:junit-platform-launcher` | `junit-platform-launcher:` |
| Checkstyle | `toolVersion =` |

### Docker Images

Read image tags from:
- `infrastructure/docker/compose/shared/docker-compose.yml` (postgres, keycloak, redis)
- `infrastructure/docker/compose/dev/docker-compose.yml` (consul, lgtm)

### Kubernetes Images

Read image tags from all manifests under `infrastructure/kubernetes/`:
- `infrastructure/postgresql/statefulset.yaml` (postgres)
- `infrastructure/redis/deployment.yaml` (redis)
- `infrastructure/consul/statefulset.yaml` (consul - 2 containers)
- `infrastructure/keycloak/deployment.yaml` (keycloak + busybox init)
- `infrastructure/lgtm/deployment.yaml` (lgtm)
- `apps/gateway/deployment.yaml` (busybox init)
- `apps/users-service/deployment.yaml` (busybox init)
- `apps/messages-service/deployment.yaml` (busybox init)

---

## Phase 2: Research Latest Stable Versions

Search for the latest **stable** release of each dependency (no RC, milestone, snapshot, or alpha/beta).

### Java / Gradle Dependencies

For each dependency, use these search strategies:

1. **GitHub releases page** of the project (most reliable)
2. **Maven Central API**: `https://search.maven.org/solrsearch/select?q=g:<groupId>+AND+a:<artifactId>&rows=5&wt=json`
3. **Web search** for `<library-name> latest version` as fallback

For Gradle plugins, check:
- `https://plugins.gradle.org/plugin/<plugin-id>`

**CRITICAL CONSTRAINT**: `protoc-protobuf` and `protoc-grpc` versions must match what the NEW `spring-grpc` BOM declares. After finding the latest `spring-grpc` version, fetch its BOM POM to extract the pinned protobuf and grpc versions:
- `https://repo1.maven.org/maven2/org/springframework/grpc/spring-grpc-dependencies/<version>/spring-grpc-dependencies-<version>.pom`

### Docker Images

For Docker Hub images, search for latest stable tags:
- Use web search: `<image-name> docker hub tags` or check the Docker Hub page directly
- For `quay.io/keycloak/keycloak`, search Quay.io or the Keycloak releases page

**Alpine tag strategy**: For `postgres` and `redis`, find the latest stable version and pair with the latest Alpine suffix. Pattern: `<version>-alpine<alpine-version>`.

**Important**: The Keycloak Docker image version and the Keycloak Java client library version are INDEPENDENT. Update them separately.

### Images to research:

| Image | Registry |
|-------|----------|
| `postgres` | Docker Hub (official) |
| `redis` | Docker Hub (official) |
| `hashicorp/consul` | Docker Hub |
| `quay.io/keycloak/keycloak` | Quay.io |
| `grafana/otel-lgtm` | Docker Hub |
| `busybox` | Docker Hub (official) |

---

## Phase 3: Present Upgrade Plan

**STOP and present the results to the user before making changes.** Show a table like:

```
## Upgrade Plan

### Java Dependencies (settings.gradle.kts)
| Key               | Current | Latest  | Action      |
|-------------------|---------|---------|-------------|
| spring-security   | X.Y.Z   | A.B.C   | UPDATE/SKIP |
| ...

### Hardcoded (build.gradle.kts)
| Artifact             | Current | Latest | Action      |
|----------------------|---------|--------|-------------|
| spotbugs-annotations | X.Y.Z   | A.B.C  | UPDATE/SKIP |
| ...

### Docker Images
| Image               | Compose  | K8s     | Latest  | Action      |
|---------------------|----------|---------|---------|-------------|
| postgres            | X.Y      | X.Y     | A.B     | UPDATE/SKIP |
| redis               | X.Y.Z    | X.Y.Z   | A.B.C   | UPDATE/SKIP |
| ...

### Pinned by spring-grpc BOM
| Dependency      | Current | Pinned to | Reason               |
|-----------------|---------|-----------|----------------------|
| protoc-protobuf | X.Y.Z   | A.B.C     | spring-grpc requires |
| protoc-grpc     | X.Y.Z   | A.B.C     | spring-grpc requires |
```

Highlight any version drift between Docker Compose and Kubernetes.

**Wait for user confirmation before proceeding to Phase 4.**

---

## Phase 4: Apply Updates

Apply in this order. Skip anything already at the latest version.

### Step 1: `settings.gradle.kts` (if scope includes `java` or `all`)

Edit each `version("key", "old")` and `plugin("key", "id").version("old")` line with the new version.

For `protoc-protobuf` and `protoc-grpc`: use the versions pinned by the NEW spring-grpc BOM. Update the comment if one exists referencing the spring-grpc version.

### Step 2: `build.gradle.kts` (if scope includes `java` or `all`)

Edit the hardcoded version strings:
- `spotbugs-annotations:<version>`
- `junit-platform-launcher:<version>`
- `toolVersion = "<version>"`

Do NOT change `JavaLanguageVersion.of(25)` unless explicitly requested.

### Step 3: Docker Compose files (if scope includes `docker` or `all`)

Update image tags in:
- `infrastructure/docker/compose/shared/docker-compose.yml` (postgres, keycloak, redis)
- `infrastructure/docker/compose/dev/docker-compose.yml` (consul, lgtm)

### Step 4: Kubernetes manifests (if scope includes `kubernetes` or `all`)

Update ALL image tags to match Docker Compose versions (fix any drift):

- `infrastructure/kubernetes/infrastructure/postgresql/statefulset.yaml`
- `infrastructure/kubernetes/infrastructure/redis/deployment.yaml`
- `infrastructure/kubernetes/infrastructure/consul/statefulset.yaml` (both container images)
- `infrastructure/kubernetes/infrastructure/keycloak/deployment.yaml` (keycloak + busybox init)
- `infrastructure/kubernetes/infrastructure/lgtm/deployment.yaml`
- `infrastructure/kubernetes/apps/gateway/deployment.yaml` (busybox init)
- `infrastructure/kubernetes/apps/users-service/deployment.yaml` (busybox init)
- `infrastructure/kubernetes/apps/messages-service/deployment.yaml` (busybox init)

### Step 5: Documentation (if scope includes `docs` or `all`)

**`README.md`:**
- Update shields.io badge URLs with new versions
- Update all inline version numbers in the Technology Stack section
- Update the Overview section version references
- Update Prerequisites section if Gradle version changed

**`CLAUDE.md` (root):**
- Update the Key Dependencies table (Version column)
- Update any version references in the Prerequisites section

**`services/CLAUDE.md`:**
- Review and update any inline version references

**`infrastructure/CLAUDE.md`:**
- Review and update any inline version references (Docker image versions, etc.)

---

## Phase 5: Build Verification

Run the build to confirm compatibility:

```bash
./gradlew clean build
```

If the build fails:
1. Read the error output
2. Identify which dependency caused the issue
3. Either revert that specific dependency with a note, or fix the compilation error
4. Re-run the build

---

## Phase 6: Summary Report

Output a final summary:

```
## Upgrade Complete

### Changes Made
**Java Dependencies**: list only changed items with old -> new
**Docker Images**: list only changed items, note any drift fixes
**Documentation**: list updated files

### Pinned / Skipped
- protoc-protobuf: pinned to X.Y.Z (required by spring-grpc)
- protoc-grpc: pinned to X.Y.Z (required by spring-grpc)
- Java 25: intentionally unchanged

### Build Result
PASSED / FAILED (with details)

### Notes
- Any breaking changes or manual fixes applied
- Any pre-release versions skipped
- Any version drift between Compose and K8s that was corrected
```

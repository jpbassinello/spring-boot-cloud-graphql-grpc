---
name: fix-spotbugs
description: Analyze and fix SpotBugs issues across the project. Runs SpotBugs, reads reports, and applies fixes following project conventions.
user-invocable: true
---

# Fix SpotBugs Issues

This skill runs SpotBugs analysis, reads the reports, and fixes identified issues following project conventions.

## Phase 1: Run SpotBugs

Run SpotBugs on the target module(s). If no specific module is mentioned, run on all subprojects.
**Always run both main and test source sets.**

```bash
# Specific module (both main and test)
./gradlew :services:grpc:users:spotbugsMain :services:grpc:users:spotbugsTest

# All modules (both main and test)
./gradlew spotbugsMain spotbugsTest
```

If the build fails due to SpotBugs violations, proceed to Phase 2.
If it succeeds, report that no issues were found.

**Note:** Reports are not generated as files. Use `--info` flag to see bug details in stdout:
```bash
./gradlew spotbugsMain spotbugsTest --info 2>&1 | grep "^[MH] [PVCD]"
```

## Phase 2: Read SpotBugs Output

This project does not configure SpotBugs report files. Instead, run with `--info` and parse stdout:

```bash
./gradlew spotbugsMain spotbugsTest --info 2>&1 | grep "^[MH] [PVCD]"
```

Output format: `<Priority> <Category> <Pattern>: <Description> At <File>:[line <N>]`

Extract:
- **Bug pattern** (e.g., `NP_NULL_ON_SOME_PATH`, `URF_UNREAD_FIELD`)
- **Class and method** where the bug was found
- **Source line number**
- **Priority** (M = Medium, H = High)

## Phase 3: Classify and Fix Issues

### Common Bug Patterns and Fixes

#### Null-Safety Issues

| Pattern | Description | Fix |
|---------|-------------|-----|
| `NP_NULL_ON_SOME_PATH` | Possible null pointer dereference | Add null check or use `Optional` |
| `NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE` | Null from method return | Check return value before use |
| `RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE` | Unnecessary null check on @NonNull | Remove redundant null check |
| `NP_NONNULL_PARAM_VIOLATION` | Passing null to @NonNull param | Ensure non-null value is passed |

**Preferred approach**: Use `@Nullable` / `@ParametersAreNonnullByDefault` from `javax.annotation` (JSR-305). This project already uses `@ParametersAreNonnullByDefault` on service classes.

```java
import javax.annotation.ParametersAreNonnullByDefault;
import jakarta.annotation.Nullable;

@ParametersAreNonnullByDefault
public class MyService {
    @Nullable
    public String findSomething() { ... }
}
```

#### Resource Management

| Pattern | Description | Fix |
|---------|-------------|-----|
| `OBL_UNSATISFIED_OBLIGATION` | Unclosed stream/connection | Use try-with-resources |
| `OS_OPEN_STREAM` | Stream not closed on all paths | Use try-with-resources |

```java
// Bad
var stream = Files.newInputStream(path);
// Good
try (var stream = Files.newInputStream(path)) { ... }
```

#### Correctness Issues

| Pattern | Description | Fix |
|---------|-------------|-----|
| `EC_UNRELATED_TYPES` | Comparing unrelated types | Fix comparison logic |
| `RV_RETURN_VALUE_IGNORED` | Return value ignored | Use or check the return value |
| `DMI_RANDOM_USED_ONLY_ONCE` | `new Random()` used once | Use `ThreadLocalRandom.current()` |
| `SE_BAD_FIELD` | Non-serializable field in Serializable class | Mark as `transient` or make serializable |

#### Performance Issues

| Pattern | Description | Fix |
|---------|-------------|-----|
| `DM_BOXED_PRIMITIVE_FOR_PARSING` | `new Integer(str)` instead of `parseInt` | Use `Integer.parseInt()` |
| `WMI_WRONG_MAP_ITERATOR` | Iterating map entries inefficiently | Use `entrySet()` instead of `keySet()` + `get()` |
| `SBSC_USE_STRINGBUFFER_CONCATENATION` | String concat in loop | Use `StringBuilder` |

#### Dodgy Code

| Pattern | Description | Fix |
|---------|-------------|-----|
| `SF_SWITCH_NO_DEFAULT` | Switch without default | Add `default` case |
| `URF_UNREAD_FIELD` | Field never read | Remove unused field |
| `DLS_DEAD_LOCAL_STORE` | Value assigned but never used | Remove assignment or use the value |
| `BC_UNCONFIRMED_CAST` | Unchecked cast | Add `instanceof` check before cast |

### Project-Specific Conventions

1. **Generated protobuf code is excluded** — already configured in `config/spotbugs/spotbugs_ignore.xml`
2. **`EI_EXPOSE_REP` and `EI_EXPOSE_REP2` are globally ignored** — Lombok `@Getter` on collections triggers these; the project accepts this trade-off
3. **`VA_FORMAT_STRING_USES_NEWLINE` is globally ignored** — `\n` is preferred over `%n` in this project
4. **`NP_NONNULL_PARAM_VIOLATION` is ignored in test code** — intentional partial construction in tests
5. **`URF_UNREAD_FIELD` is ignored in test code** — Mockito `@Spy`/`@InjectMocks` reads fields via reflection, invisible to SpotBugs
6. **Use SpotBugs annotations for intentional suppressions**:

```java
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE",
    justification = "Defensive null check for external API response")
```

Only suppress when the fix would hurt readability or when the warning is a false positive. Always include `justification`.

## Phase 4: Handle False Positives

If a SpotBugs finding is a false positive:

### Option 1: Suppress on the specific method/class (preferred for isolated cases)

```java
@SuppressFBWarnings(value = "PATTERN_CODE", justification = "Reason")
```

### Option 2: Add to global ignore filter (for patterns that are always false positives)

Edit `config/spotbugs/spotbugs_ignore.xml`:

```xml
<FindBugsFilter>
    <Match>
        <Or>
            <Bug pattern="EXISTING_PATTERN"/>
            <Bug pattern="NEW_PATTERN_TO_IGNORE"/>
        </Or>
    </Match>
</FindBugsFilter>
```

Use Option 2 sparingly — only for patterns that are consistently false positives project-wide.

## Phase 5: Verify Fixes

After applying fixes:

```bash
# Re-run SpotBugs to confirm issues are resolved (both main and test)
./gradlew spotbugsMain spotbugsTest

# Run tests to ensure fixes don't break anything
./gradlew test

# Run checkstyle to ensure fixes comply with code style
./gradlew checkstyleMain
```
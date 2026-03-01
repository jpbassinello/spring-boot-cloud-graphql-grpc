# Automating Dependency Upgrades with Claude Code Skills

This is the fourth post in the SBCGG series. If you haven't read the previous ones, check them
out: [Introducing SBCGG](1-About-SBCGG.md), [gRPC in SBCGG](2-gRPC-in-SBCGG.md),
and [Testcontainers with Gradle Test Fixtures](3-Testcontainers-with-Gradle-Test-Fixtures.md).

**GitHub**: https://github.com/jpbassinello/spring-boot-cloud-graphql-grpc

---

## The Problem: Dependency Upgrades Are a Time Sink

Every developer knows the drill. You open your project after a few weeks and realize your dependencies are outdated.
What follows is a tedious, error-prone process:

1. **Check each dependency** for a newer version. Open Maven Central, Docker Hub, GitHub releases, Gradle Plugin Portal.
   One by one.
2. **Understand constraints**. Does the new Spring gRPC version pin a specific protobuf version? Is the Keycloak Docker
   image version the same as the Java client library version? (Spoiler: it's not.)
3. **Update Gradle files**. Version catalogs, hardcoded versions, plugin versions.
4. **Update Docker Compose files**. Match the image tags.
5. **Update Kubernetes manifests**. Hope they haven't drifted from Docker Compose. (Spoiler: they have.)
6. **Update documentation**. README badges, technology stack tables, inline version references across multiple files.
7. **Run the build**. Fix whatever broke. Run it again.

For SBCGG, this means touching **11 Java/Gradle dependencies**, **5 Gradle plugins**, **3 hardcoded versions**, **6
Docker images** across **2 Compose files** and **8 Kubernetes manifests**, plus **4 documentation files**. That's
roughly **20+ files** and **30+ version numbers** to research, verify, and update.

Manually, this takes anywhere from **1 to 2 hours** of focused work. Not because it's hard, but because it's scattered,
repetitive, and requires context-switching between Maven Central tabs, Docker Hub searches, and your IDE.

## The Solution: A Claude Code Skill

[Claude Code](https://docs.anthropic.com/en/docs/claude-code/overview) is Anthropic's agentic coding tool that lives in
your terminal. It can read files, edit code, search the web, and run commands. But the real power comes from **skills**:
reusable prompt files that encode domain-specific knowledge and workflows.

A skill is just a markdown file that tells Claude *exactly* what to do, step by step. Think of it as a runbook that
Claude can execute autonomously. You define the process once, and then invoke it whenever you need it.

For SBCGG, I created a skill called `upgrade-deps` that handles the entire dependency upgrade workflow:

```
.claude/skills/upgrade-deps/SKILL.md
```

### How It Works

The skill is structured in six phases:

**Phase 1: Catalog Current Versions** - Read all version numbers from every source: `settings.gradle.kts`,
`build.gradle.kts`, Docker Compose files, and Kubernetes manifests. The skill specifies exactly which files to read and
what to look for.

**Phase 2: Research Latest Stable Versions** - Search for the latest stable release of each dependency using Maven
Central, GitHub releases, Docker Hub, and the Gradle Plugin Portal. The skill includes a critical constraint: protobuf
and gRPC versions must be extracted from the Spring gRPC BOM POM, not looked up independently.

**Phase 3: Present Upgrade Plan** - Stop and show the developer a table of all proposed changes before touching any
files. This is the human checkpoint.

**Phase 4: Apply Updates** - Edit all files in a specific order: Gradle configs first, then Docker Compose, then
Kubernetes (fixing any drift), then documentation.

**Phase 5: Build Verification** - Run `./gradlew clean build` and handle failures by reverting problematic dependencies.

**Phase 6: Summary Report** - Output a structured summary of everything that changed.

### What the Skill File Looks Like

Here's a simplified excerpt from the skill:

```markdown
## Phase 2: Research Latest Stable Versions

Search for the latest **stable** release of each dependency
(no RC, milestone, snapshot, or alpha/beta).

**CRITICAL CONSTRAINT**: `protoc-protobuf` and `protoc-grpc` versions
must match what the NEW `spring-grpc` BOM declares. After finding the
latest `spring-grpc` version, fetch its BOM POM to extract the pinned
protobuf and grpc versions:

https://repo1.maven.org/maven2/org/springframework/grpc/
spring-grpc-dependencies/<version>/spring-grpc-dependencies-<version>.pom
```

This is the kind of domain knowledge that lives in a developer's head and gets lost between upgrade sessions. By
encoding it in the skill, Claude follows the same process every time.

### Running It

From the terminal, you just type:

```
claude
> /upgrade-deps
```

That's it. Claude reads all the files, researches every dependency in parallel, presents a table of proposed changes,
waits for your approval, applies all edits, runs the build, and reports the results.

## A Real Example

Here's what happened when I ran `/upgrade-deps` on SBCGG:

### The Upgrade Plan

Claude presented this table (abbreviated):

| Dependency         | Current | Latest | Action                             |
|--------------------|---------|--------|------------------------------------|
| spring-security    | 7.0.2   | 7.0.3  | UPDATE                             |
| spring-boot plugin | 4.0.2   | 4.0.3  | UPDATE                             |
| buf plugin         | 0.8.5   | 0.11.0 | UPDATE                             |
| protoc-protobuf    | 4.33.2  | 4.33.4 | UPDATE (pinned by spring-grpc BOM) |
| checkstyle         | 12.1.0  | 13.2.0 | UPDATE                             |
| keycloak client    | 26.0.8  | 26.0.8 | SKIP (already latest)              |

It also detected **version drift** in Kubernetes manifests:

| Image    | Compose          | K8s                  | Latest           |
|----------|------------------|----------------------|------------------|
| redis    | 8.6.0-alpine3.23 | **8.4.0-alpine3.22** | 8.6.1-alpine3.23 |
| keycloak | 26.5.3           | **26.5.2**           | 26.5.4           |
| lgtm     | 0.18.1           | **0.13.0**           | 0.19.1           |

I would not have caught the LGTM image being **six minor versions behind** in Kubernetes without this automated check.

### The Result

After I approved the plan, Claude:

- Updated **5 version entries** in `settings.gradle.kts`
- Updated **2 hardcoded versions** in `build.gradle.kts`
- Updated **5 Docker images** in Compose files
- Updated **8 Kubernetes manifests** (fixing drift)
- Updated **15+ version references** across `README.md` and `CLAUDE.md`
- Ran the build: **BUILD SUCCESSFUL in 33s**

One thing worth noting: the build initially failed because the Keycloak admin client version `26.5.4` doesn't exist on
Maven Central. The Docker image follows a different release cadence than the Java library. Claude caught the build
failure, identified the root cause, reverted to `26.0.8`, and rebuilt successfully. This is exactly the kind of subtle
constraint that trips developers up during manual upgrades.

## Why This Matters

### Time Saved

The entire process, from invocation to successful build, takes about **5 minutes** with the skill. The same work
manually takes **1 to 2 hours**. That's not a one-time saving. Dependency upgrades should happen regularly, ideally
every few weeks, so this compounds quickly.

### Documentation Stays in Sync

This is the part that usually gets skipped. When you manually upgrade Spring Boot from 4.0.2 to 4.0.3, do you also
update the README badge? The Technology Stack table? The Overview section that mentions the version? The CLAUDE.md
dependency table?

Most developers update the code and forget the docs. The skill treats documentation as a first-class citizen, updating
every version reference across every file, every time.

### Drift Detection

When you have the same image referenced in Docker Compose and Kubernetes manifests, drift is inevitable. Someone updates
Compose but forgets Kubernetes, or vice versa. The skill compares versions across both environments and fixes
discrepancies automatically.

### Institutional Knowledge is Encoded

The constraint that `protoc-protobuf` must match the Spring gRPC BOM is the kind of knowledge that exists in one
developer's head. When that developer leaves, the next person will spend time debugging why the build broke after a
protobuf upgrade. The skill encodes this knowledge permanently.

## How to Create Your Own Skill

Claude Code skills are markdown files stored in `.claude/skills/`. The structure is:

```
.claude/skills/
└── upgrade-deps/
    └── SKILL.md
```

The `SKILL.md` file starts with YAML frontmatter:

```yaml
---
name: upgrade-deps
description: Upgrade project dependencies, Docker images, and
  documentation versions.
disable-model-invocation: true
argument-hint: "[all|java|docker|kubernetes|docs]"
---
```

The rest is plain markdown with instructions. The key principles for writing effective skills:

1. **Be specific about file paths**. Don't say "update Kubernetes manifests". List every file path explicitly.
2. **Encode constraints**. If two versions must be in sync, say so and explain how to verify.
3. **Include human checkpoints**. The skill stops after Phase 3 to show the upgrade plan before making changes.
4. **Handle failures**. Tell Claude what to do when the build breaks.
5. **Define the output format**. A structured summary report makes it easy to review what happened.

## The Bigger Picture

Dependency upgrades are just one example. Any repetitive, multi-step process that follows a predictable pattern is a
good candidate for a skill:

- **Release process**: bump version, update changelog, tag, build, publish
- **New service scaffolding**: create directories, copy templates, wire up dependencies
- **Security audit**: check for vulnerable dependencies, update, verify

The pattern is always the same: take the runbook out of your head (or your wiki), put it in a markdown file, and let
Claude execute it. The developer stays in control through human checkpoints, but the tedious parts are automated.

---

## What's Next?

In the next posts, I plan to cover:

- **How Spring Boot 4 simplified observability** and why metrics and traces are essential in microservices
- **Hexagonal Architecture in production microservices projects**

---

I'd love to hear your feedback. If you think this could help other developers, please share it.

**GitHub**: https://github.com/jpbassinello/spring-boot-cloud-graphql-grpc

Thank you for reading.

---
name: new-microservice
description: Step-by-step guide for adding a new gRPC microservice to the project. Use when creating a new service module with hexagonal architecture.
user-invocable: true
---

# Adding a New Microservice

## Step 1: Create gRPC Interface

Create proto file in `grpc-interfaces/<service-name>/src/main/proto/<service>.proto`:

```protobuf
syntax = "proto3";
package br.com.jpbassinello.sbcgg.<service>;

service <Service>Service {
  rpc Create<Entity>(Create<Entity>Request) returns (Create<Entity>Response);
}

message Create<Entity>Request {
  string field1 = 1;
}

message Create<Entity>Response {
  string id = 1;
}
```

## Step 2: Create Service Module

```bash
mkdir -p services/grpc/<service>/src/main/java/br/com/jpbassinello/sbcgg/<service>
```

## Step 3: Implement Hexagonal Structure

```
services/grpc/<service>/
├── domain/
│   └── model/
│       └── <Entity>.java
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   └── Create<Entity>UseCase.java
│   │   └── out/
│   │       └── <Entity>Repository.java
│   └── service/
│       └── <Entity>Service.java
└── adapter/
    ├── in/
    │   └── grpc/
    │       └── <Entity>GrpcController.java
    └── out/
        └── persistence/
            └── <Entity>JpaAdapter.java
```

## Step 4: Add Dependencies

```kotlin
// services/grpc/<service>/build.gradle.kts
dependencies {
    api(project(":grpc-interfaces:<service>"))
    api(project(":shared:grpc-server"))
    api(project(":shared:spring-jpa"))
}
```

## Step 5: Add Configuration

```yaml
# services/grpc/<service>/src/main/resources/application.yml
spring:
  application:
    name: <service>-service
  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        enabled: true
        prefer-ip-address: true
grpc:
  server:
    port: <port>
```

## Step 6: Register in settings.gradle.kts

Add the new modules:
```kotlin
include(":grpc-interfaces:<service>")
include(":services:grpc:<service>")
```

## Step 7: Write Tests

- Create unit tests for domain and application layers
- Create integration tests for adapters
- See `.claude/rules/testing.md` for test templates

## Updating gRPC Definitions

When modifying existing `.proto` files:

```bash
# 1. Modify .proto files in grpc-interfaces/<service>/src/main/proto/
# 2. Rebuild the interface module
./gradlew :grpc-interfaces:<service>:build

# 3. Rebuild services that depend on it
./gradlew :services:grpc:<service>:build
```

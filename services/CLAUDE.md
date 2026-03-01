# Services Development Guide

## Starting the Application Locally

Services must be started in order due to dependencies.

**Step 1: Start Infrastructure**

```bash
cd infrastructure/docker/compose/dev
docker-compose up -d
cd -
```

This starts: PostgreSQL, Redis, Consul, Keycloak, Grafana LGTM.

Consul automatically loads service configurations from YAML files during initialization via `init.sh`.

**Step 2: Start Microservices**

```bash
./gradlew :services:grpc:users:bootRun      # Terminal 1
./gradlew :services:grpc:messages:bootRun    # Terminal 2
```

When running locally (`dev` profile), services do not connect to Consul. Consul integration is enabled only with `dev-docker` profile.

**Step 3: Start API Gateway**

```bash
./gradlew :services:graphql:gateway:bootRun
```

## Adding a New Microservice

**Step 1: Create gRPC Interface** in `grpc-interfaces/<service-name>/src/main/proto/`

**Step 2: Define Protocol Buffer Schema**

```protobuf
syntax = "proto3";
package br.com.jpbassinello.sbcgg.orders;

service OrdersService {
  rpc CreateOrder(CreateOrderRequest) returns (CreateOrderResponse);
}

message CreateOrderRequest {
  string customer_id = 1;
  repeated OrderItem items = 2;
}

message CreateOrderResponse {
  string order_id = 1;
}
```

**Step 3: Create Service Module**

```bash
mkdir -p services/grpc/orders/src/main/java/br/com/jpbassinello/sbcgg/orders
```

**Step 4: Implement Hexagonal Structure**

```
services/grpc/orders/
├── domain/
│   └── model/
│       └── Order.java
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   └── CreateOrderUseCase.java
│   │   └── out/
│   │       └── OrderRepository.java
│   └── service/
│       └── OrderService.java
└── adapter/
    ├── in/
    │   └── grpc/
    │       └── OrdersGrpcController.java
    └── out/
        └── persistence/
            └── OrderJpaAdapter.java
```

**Step 5: Add Dependencies**

```kotlin
// services/grpc/orders/build.gradle.kts
dependencies {
    api(project(":grpc-interfaces:orders"))
    api(project(":shared:grpc-server"))
    api(project(":shared:spring-jpa"))
}
```

**Step 6: Add Configuration**

```yaml
# services/grpc/orders/src/main/resources/application.yml
spring:
  application:
    name: orders-service
  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        enabled: true
        prefer-ip-address: true
grpc:
  server:
    port: 9093
```

**Step 7: Write Tests** - Unit tests for domain/application layers, integration tests for adapters.

## Consul Configuration Management

Consul provides service discovery and centralized configuration via its KV store.

**Configuration files**: `infrastructure/docker/compose/configs/consul/yaml/`
- `users-service.yaml`, `messages-service.yaml`

**How it works**:
1. Docker Compose startup runs Consul's `init.sh` script
2. Script loads YAML files into KV store with keys like `config/{service-name},{profile}/data`
3. Services connect to Consul and retrieve configuration on startup

**Consul config is enabled in** `shared/spring-app/src/main/resources/config/app/application-dev-docker.yaml`:

```yaml
spring:
  cloud:
    consul:
      host: consul
      config:
        enabled: true
        format: YAML
        fail-fast: true
      discovery:
        enabled: true
        register: true
        fail-fast: true
```

**Accessing Configuration**:

```bash
curl http://localhost:8500/v1/kv/config/users-service,dev/data  # Get config
curl http://localhost:8500/v1/kv/?keys                          # View all keys
```

**Profile Behavior**:
- **dev** (local): Consul disabled, uses local configuration files
- **dev-docker** (Docker Compose): Consul enabled, retrieves config from KV store

### Adding Consul Configuration for a New Service

1. Create config file in `infrastructure/docker/compose/configs/consul/yaml/`

```yaml
# infrastructure/docker/compose/configs/consul/yaml/orders-service.yaml
spring:
  datasource:
    password: postgres
keycloak:
  password: keycloak
```

2. Update `infrastructure/docker/compose/configs/consul/init.sh`:

```bash
consul kv put config/orders-service,dev/data @/tmp/yaml/orders-service.yaml
consul kv put config/orders-service,dev-docker/data @/tmp/yaml/orders-service.yaml
```

3. Restart Consul: `docker restart sbcgg-consul`

### Configuration Layers

1. **Shared**: `shared/spring-app/src/main/resources/config/app/` - Common settings
2. **Service-specific**: `services/{service}/src/main/resources/config/`
3. **Consul KV store**: `infrastructure/docker/compose/configs/consul/yaml/` - Secrets/overrides

**Best Practices**:
- Store sensitive data (passwords, tokens) in Consul KV store
- Use profiles for different environments (dev, dev-docker, prod)
- Keep non-sensitive defaults in service configuration files
- Don't commit production passwords to Consul YAML files

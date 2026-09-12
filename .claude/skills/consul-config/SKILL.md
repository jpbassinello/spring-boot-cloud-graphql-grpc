---
name: consul-config
description: Guide for managing Consul configuration, adding service configs, and troubleshooting Consul KV store.
user-invocable: true
---

# Consul Configuration Management

Consul provides both service discovery and centralized configuration management using its Key/Value (KV) store.

## Configuration Storage

Configuration files are stored in `infrastructure/docker/compose/configs/consul/yaml/`

**Configuration Files**:
- `users-service.yaml` - Users service configuration
- `messages-service.yaml` - Messages service configuration

## How It Works

1. During Docker Compose startup, Consul runs the `init.sh` script
2. The script loads YAML files into Consul's KV store using keys like `config/{service-name},{profile}/data`
3. Services connect to Consul and retrieve their configuration on startup

## Service Configuration

Services enable Consul config in `shared/spring-app/src/main/resources/config/app/application-dev-docker.yaml`:

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

## Profile Behavior

- **dev profile** (local development): Consul is disabled, services use local configuration files
- **dev-docker profile** (Docker Compose): Consul is enabled, services retrieve configuration from Consul KV store

## Adding Configuration for a New Service

1. Create a configuration file in `infrastructure/docker/compose/configs/consul/yaml/`:

```yaml
# infrastructure/docker/compose/configs/consul/yaml/<service>-service.yaml
spring:
  datasource:
    password: postgres

keycloak:
  password: keycloak
```

2. Update the init script to load the new configuration:

```bash
# Edit infrastructure/docker/compose/configs/consul/init.sh
consul kv put config/<service>-service,dev/data @/tmp/yaml/<service>-service.yaml
consul kv put config/<service>-service,dev-docker/data @/tmp/yaml/<service>-service.yaml
```

3. Restart Consul container:

```bash
docker restart sbcgg-consul
```

## Updating Existing Configuration

```bash
# Option 1: Update manually
consul kv put config/users-service,dev/data @infrastructure/docker/compose/configs/consul/yaml/users-service.yaml

# Option 2: Restart Consul (reloads all configs via init script)
docker restart sbcgg-consul
```

Then restart affected services to pick up new configuration.

## Configuration Layering

Services use a layered configuration approach:

1. **Shared configuration**: `shared/spring-app/src/main/resources/config/app/` - Common settings for all services
2. **Service configuration**: `services/{service}/src/main/resources/config/` - Service-specific settings
3. **Consul KV store**: `infrastructure/docker/compose/configs/consul/yaml/` - Environment-specific secrets and overrides

## Accessing Configuration

```bash
# Get configuration from Consul KV store
curl http://localhost:8500/v1/kv/config/users-service,dev/data?raw

# View all configuration keys
curl http://localhost:8500/v1/kv/?keys

# View Consul UI
open http://localhost:8500

# Check if Consul config is loaded in a service
curl http://localhost:8090/actuator/env | jq '.propertySources[] | select(.name | contains("consul"))'
```

## Best Practices

- Store **sensitive data** in Consul KV store (passwords, tokens)
- Use **profiles** for different environments (dev, dev-docker, prod)
- Keep **non-sensitive defaults** in service configuration files
- Use local configuration files for the `dev` profile (no Consul dependency)
- Use Consul configuration for the `dev-docker` profile (Docker environment)
- Don't commit **production passwords** to Consul YAML files in version control
- Don't store **service logic** in configuration

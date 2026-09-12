    echo "Waiting for Consul to be ready..."
    until consul members; do
      sleep 1
    done
    echo "Consul is ready. Initializing KV pairs..."

    consul kv put config/application,dev/data @/tmp/yaml/application.yaml
    consul kv put config/application,dev-docker/data @/tmp/yaml/application.yaml

    consul kv put config/users-service,dev/data @/tmp/yaml/users-service.yaml
    consul kv put config/users-service,dev-docker/data @/tmp/yaml/users-service.yaml

    consul kv put config/messages-service,dev/data @/tmp/yaml/messages-service.yaml
    consul kv put config/messages-service,dev-docker/data @/tmp/yaml/messages-service.yaml

    consul kv put config/graphql-gateway,dev/data @/tmp/yaml/graphql-gateway.yaml
    consul kv put config/graphql-gateway,dev-docker/data @/tmp/yaml/graphql-gateway.yaml

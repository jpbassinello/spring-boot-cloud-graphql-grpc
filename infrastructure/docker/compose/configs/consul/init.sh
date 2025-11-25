    # init-consul.sh
    #!/bin/sh
    echo "Waiting for Consul to be ready..."
    until consul members; do
      sleep 1
    done
    echo "Consul is ready. Initializing KV pairs..."
    consul kv put config/users-service,dev/data @/tmp/yaml/users-service.yaml
    consul kv put config/users-service,dev-docker/data @/tmp/yaml/users-service.yaml
    consul kv put config/messages-service,dev/data @/tmp/yaml/messages-service.yaml
    consul kv put config/messages-service,dev-docker/data @/tmp/yaml/messages-service.yaml
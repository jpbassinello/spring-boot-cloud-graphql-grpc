#!/bin/sh
# init-consul.sh (prod) — seeds KV only when keys do not already exist.
#
# Unlike the dev init script, this one never overwrites. The committed YAML files hold
# placeholder *expressions* (`${POSTGRES_PASSWORD:postgres}`) resolved from each container's
# environment, so an unconditional `consul kv put` on every restart would be harmless — but a
# key an operator edited by hand (`consul kv put ...`) would be silently reverted to the
# committed default on the next deploy.
echo "Waiting for Consul to be ready..."
until consul members; do
  sleep 1
done
echo "Consul is ready. Initializing KV pairs (skip existing)..."

put_if_absent() {
  key="$1"
  file="$2"
  if consul kv get "$key" > /dev/null 2>&1; then
    echo "  SKIP $key (already exists)"
  else
    consul kv put "$key" @"$file"
    echo "  SEED $key"
  fi
}

put_if_absent config/application,prod-docker/data /tmp/yaml/application.yaml
put_if_absent config/users-service,prod-docker/data /tmp/yaml/users-service.yaml
put_if_absent config/messages-service,prod-docker/data /tmp/yaml/messages-service.yaml
put_if_absent config/graphql-gateway,prod-docker/data /tmp/yaml/graphql-gateway.yaml

echo "KV initialization complete."

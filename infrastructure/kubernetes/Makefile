# Makefile for Kubernetes Deployment

.PHONY: help deploy deploy-infra deploy-apps deploy-ingress delete status logs shell build-images push-images

# Default namespace
NAMESPACE ?= sbcgg

# Docker registry
REGISTRY ?= your-registry

# Image tag
TAG ?= latest

help: ## Show this help message
	@echo 'Usage: make [target]'
	@echo ''
	@echo 'Available targets:'
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  %-20s %s\n", $$1, $$2}' $(MAKEFILE_LIST)

check-prerequisites: ## Check if required tools are installed
	@echo "Checking prerequisites..."
	@command -v kubectl >/dev/null 2>&1 || { echo "kubectl is required but not installed. Aborting." >&2; exit 1; }
	@command -v docker >/dev/null 2>&1 || { echo "docker is required but not installed. Aborting." >&2; exit 1; }
	@echo "✓ All prerequisites satisfied"

build-images: ## Build Docker images for all services
	@echo "Building Docker images..."
	cd ../.. && ./gradlew build docker
	@echo "✓ Images built successfully"

tag-images: ## Tag Docker images for registry
	@echo "Tagging images for registry $(REGISTRY)..."
	docker tag sbcgg/users:latest $(REGISTRY)/sbcgg/users:$(TAG)
	docker tag sbcgg/messages:latest $(REGISTRY)/sbcgg/messages:$(TAG)
	docker tag sbcgg/gateway:latest $(REGISTRY)/sbcgg/gateway:$(TAG)
	@echo "✓ Images tagged successfully"

push-images: tag-images ## Push Docker images to registry
	@echo "Pushing images to $(REGISTRY)..."
	docker push $(REGISTRY)/sbcgg/users:$(TAG)
	docker push $(REGISTRY)/sbcgg/messages:$(TAG)
	docker push $(REGISTRY)/sbcgg/gateway:$(TAG)
	@echo "✓ Images pushed successfully"

create-namespace: ## Create the namespace
	kubectl apply -f base/namespace.yaml

deploy: check-prerequisites ## Deploy everything to Kubernetes
	@echo "Deploying to Kubernetes..."
	kubectl apply -k .
	@echo "✓ Deployment complete!"
	@echo ""
	@echo "Check deployment status with: make status"
	@echo "View logs with: make logs"

deploy-infra: create-namespace ## Deploy only infrastructure services
	@echo "Deploying infrastructure services..."
	kubectl apply -f config/
	kubectl apply -f infrastructure/
	@echo "✓ Infrastructure deployed"

deploy-apps: ## Deploy only application services
	@echo "Deploying application services..."
	kubectl apply -f apps/
	@echo "✓ Applications deployed"

deploy-ingress: ## Deploy ingress configuration
	@echo "Deploying ingress..."
	kubectl apply -f ingress/
	@echo "✓ Ingress deployed"

delete: ## Delete all resources
	@echo "Deleting all resources from namespace $(NAMESPACE)..."
	kubectl delete namespace $(NAMESPACE)
	@echo "✓ All resources deleted"

status: ## Show status of all resources
	@echo "=== Namespace ==="
	kubectl get namespace $(NAMESPACE)
	@echo ""
	@echo "=== Pods ==="
	kubectl get pods -n $(NAMESPACE)
	@echo ""
	@echo "=== Services ==="
	kubectl get svc -n $(NAMESPACE)
	@echo ""
	@echo "=== Deployments ==="
	kubectl get deployments -n $(NAMESPACE)
	@echo ""
	@echo "=== StatefulSets ==="
	kubectl get statefulsets -n $(NAMESPACE)
	@echo ""
	@echo "=== HPA ==="
	kubectl get hpa -n $(NAMESPACE)
	@echo ""
	@echo "=== Ingress ==="
	kubectl get ingress -n $(NAMESPACE)

logs: ## Show logs from gateway (use SERVICE=users-service to change)
	@SERVICE ?= gateway
	kubectl logs -n $(NAMESPACE) -l app=$(SERVICE) --tail=100 -f

logs-all: ## Show logs from all services
	kubectl logs -n $(NAMESPACE) --all-containers=true --tail=50

shell: ## Open shell in gateway pod (use SERVICE=users-service to change)
	@SERVICE ?= gateway
	kubectl exec -it -n $(NAMESPACE) $$(kubectl get pod -n $(NAMESPACE) -l app=$(SERVICE) -o jsonpath='{.items[0].metadata.name}') -- sh

shell-postgres: ## Open PostgreSQL shell
	kubectl exec -it -n $(NAMESPACE) postgresql-0 -- psql -U postgres -d users

shell-redis: ## Open Redis CLI
	kubectl exec -it -n $(NAMESPACE) $$(kubectl get pod -n $(NAMESPACE) -l app=redis -o jsonpath='{.items[0].metadata.name}') -- redis-cli -a redis

shell-consul: ## Open Consul shell
	kubectl exec -it -n $(NAMESPACE) consul-0 -- sh

port-forward-gateway: ## Port forward gateway to localhost:8080
	kubectl port-forward -n $(NAMESPACE) svc/gateway 8080:8080

port-forward-grafana: ## Port forward Grafana to localhost:3000
	kubectl port-forward -n $(NAMESPACE) svc/lgtm 3000:3000

port-forward-consul: ## Port forward Consul to localhost:8500
	kubectl port-forward -n $(NAMESPACE) svc/consul 8500:8500

port-forward-keycloak: ## Port forward Keycloak to localhost:9090
	kubectl port-forward -n $(NAMESPACE) svc/keycloak 9090:8080

describe-pod: ## Describe a pod (use POD=pod-name)
	@POD ?= gateway
	kubectl describe pod -n $(NAMESPACE) -l app=$(POD)

restart-deployment: ## Restart a deployment (use SERVICE=gateway)
	@SERVICE ?= gateway
	kubectl rollout restart deployment $(SERVICE) -n $(NAMESPACE)

scale: ## Scale a deployment (use SERVICE=gateway REPLICAS=3)
	@SERVICE ?= gateway
	@REPLICAS ?= 2
	kubectl scale deployment $(SERVICE) -n $(NAMESPACE) --replicas=$(REPLICAS)

rollback: ## Rollback a deployment (use SERVICE=gateway)
	@SERVICE ?= gateway
	kubectl rollout undo deployment $(SERVICE) -n $(NAMESPACE)

watch: ## Watch pod status
	watch kubectl get pods -n $(NAMESPACE)

top: ## Show resource usage
	kubectl top pods -n $(NAMESPACE)
	kubectl top nodes

events: ## Show recent events
	kubectl get events -n $(NAMESPACE) --sort-by='.lastTimestamp'

debug: ## Run debug pod for troubleshooting
	kubectl run -it --rm debug --image=busybox --restart=Never -n $(NAMESPACE) -- sh

test-connectivity: ## Test service connectivity from debug pod
	kubectl run -it --rm test --image=curlimages/curl --restart=Never -n $(NAMESPACE) -- \
		curl -v http://gateway.$(NAMESPACE).svc.cluster.local:8080/actuator/health

apply-secrets: ## Apply secrets (make sure to update config/secrets.yaml first!)
	@echo "⚠️  WARNING: This will update secrets in the cluster"
	@echo "Make sure you have updated config/secrets.yaml with production values!"
	@read -p "Continue? (y/N): " confirm && [ "$$confirm" = "y" ] || exit 1
	kubectl apply -f config/secrets.yaml

generate-consul-key: ## Generate Consul gossip encryption key
	@echo "Generating Consul gossip key..."
	@docker run --rm hashicorp/consul:1.22.1 consul keygen

validate: ## Validate Kubernetes manifests
	@echo "Validating manifests..."
	kubectl apply -k . --dry-run=client
	@echo "✓ Manifests are valid"

diff: ## Show diff of what would change
	kubectl diff -k .

context: ## Show current kubectl context
	kubectl config current-context
	kubectl config get-contexts

switch-context: ## Switch kubectl context (use CONTEXT=context-name)
	@CONTEXT ?= minikube
	kubectl config use-context $(CONTEXT)

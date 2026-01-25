# Kubernetes Deployment Guide

This directory contains Kubernetes manifests for deploying the SBCGG (Spring Boot Cloud GraphQL gRPC) microservices
platform to a Kubernetes cluster.

## Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Directory Structure](#directory-structure)
- [Configuration](#configuration)
- [Deployment](#deployment)
- [Accessing Services](#accessing-services)
- [Monitoring & Observability](#monitoring--observability)
- [Scaling](#scaling)
- [Production Considerations](#production-considerations)
- [Troubleshooting](#troubleshooting)

## Overview

The Kubernetes deployment includes:

- **Infrastructure Services**: PostgreSQL, Redis, Consul, Keycloak, Grafana LGTM
- **Microservices**: Users Service, Messages Service, GraphQL Gateway
- **Ingress**: NGINX Ingress Controller for external access
- **Auto-scaling**: Horizontal Pod Autoscalers (HPA) for microservices
- **Observability**: Distributed tracing with OpenTelemetry and Grafana LGTM stack

## Prerequisites

Before deploying, ensure you have:

1. **Kubernetes Cluster** (v1.28+)
    - Local: [Minikube](https://minikube.sigs.k8s.io/), [Kind](https://kind.sigs.k8s.io/),
      or [Docker Desktop](https://www.docker.com/products/docker-desktop)
    - Cloud: GKE, EKS, AKS, or other managed Kubernetes service

2. **kubectl** (v1.28+)
   ```bash
   kubectl version --client
   ```

3. **kustomize** (v5.0+) - Optional, kubectl has built-in kustomize support
   ```bash
   kustomize version
   ```

4. **NGINX Ingress Controller**
   ```bash
   # For Minikube
   minikube addons enable ingress

   # For other clusters
   kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/cloud/deploy.yaml
   ```

5. **Metrics Server** (for HPA)
   ```bash
   # For Minikube
   minikube addons enable metrics-server

   # For other clusters
   kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
   ```

6. **Docker Images**
    - Build and push your Docker images to a registry:
   ```bash
   # Build images
   ./gradlew build docker

   # Tag images for your registry
   docker tag sbcgg/users:latest your-registry/sbcgg/users:latest
   docker tag sbcgg/messages:latest your-registry/sbcgg/messages:latest
   docker tag sbcgg/gateway:latest your-registry/sbcgg/gateway:latest

   # Push to registry
   docker push your-registry/sbcgg/users:latest
   docker push your-registry/sbcgg/messages:latest
   docker push your-registry/sbcgg/gateway:latest
   ```

## Quick Start

### 1. Clone and Navigate

```bash
cd infrastructure/kubernetes
```

### 2. Update Secrets (IMPORTANT!) - (not required when running local)

Edit `config/secrets.yaml` and change the default passwords:

```bash
# Generate a strong Consul gossip key
consul keygen

# Update secrets.yaml with the generated key and strong passwords
kubectl create secret generic sbcgg-secrets \
  --from-literal=POSTGRES_PASSWORD=your-strong-password \
  --from-literal=REDIS_PASSWORD=your-strong-password \
  --from-literal=KEYCLOAK_ADMIN_PASSWORD=your-strong-password \
  --from-literal=JWT_SECRET=your-jwt-secret \
  --dry-run=client -o yaml > config/secrets.yaml
```

### 3. Update Image References (not required when running local)

Edit deployment files to use your registry:

- `apps/users-service/deployment.yaml`
- `apps/messages-service/deployment.yaml`
- `apps/gateway/deployment.yaml`

Change `image: sbcgg/service:latest` to `image: your-registry/sbcgg/service:latest`

### 4. Deploy Everything

```bash
# Using kubectl with kustomize
kubectl apply -k .

# Or deploy step by step
kubectl apply -f base/namespace.yaml
kubectl apply -f config/
kubectl apply -f infrastructure/
kubectl apply -f apps/
kubectl apply -f ingress/
```

### 5. Wait for Services to Start

```bash
# Watch pod status
kubectl get pods -n sbcgg -w

# Check all resources
kubectl get all -n sbcgg
```

### 6. Configure Local DNS (for local development)

Add to your `/etc/hosts` (or `C:\Windows\System32\drivers\etc\hosts` on Windows):

```
127.0.0.1 sbcgg.local
127.0.0.1 grafana.sbcgg.local
127.0.0.1 consul.sbcgg.local
127.0.0.1 keycloak.sbcgg.local
```

For Minikube, use the Minikube IP:

```bash
minikube ip  # Use this IP instead of 127.0.0.1
```

## Directory Structure

```
kubernetes/
├── base/                       # Base resources
│   ├── namespace.yaml         # Namespace definition
│   └── kustomization.yaml     # Base kustomization
│
├── config/                    # Configuration
│   ├── configmap.yaml        # Application configuration
│   └── secrets.yaml          # Sensitive data (change defaults!)
│
├── infrastructure/            # Infrastructure services
│   ├── postgresql/           # PostgreSQL StatefulSet
│   ├── redis/                # Redis deployment
│   ├── consul/               # Consul service discovery
│   ├── keycloak/             # Keycloak identity management
│   └── lgtm/                 # Grafana LGTM observability stack
│
├── apps/                      # Application microservices
│   ├── users-service/        # Users microservice
│   ├── messages-service/     # Messages microservice
│   └── gateway/              # GraphQL API gateway
│
├── ingress/                   # Ingress configuration
│   └── ingress.yaml          # NGINX ingress rules
│
├── kustomization.yaml         # Root kustomization file
└── README.md                  # This file
```

## Configuration

### Environment Variables

Configuration is managed through ConfigMaps and Secrets:

**ConfigMap** (`config/configmap.yaml`):

- Database hosts and ports
- Service endpoints
- Non-sensitive configuration

**Secrets** (`config/secrets.yaml`):

- Database passwords
- API keys
- JWT secrets

### Spring Profiles

The Kubernetes deployment uses the `dev-docker` Spring profile. Create corresponding configuration files:

## Deployment

### Deploy All Resources

```bash
kubectl apply -k .
```

### Deploy Individual Components

```bash
# Infrastructure only
kubectl apply -f base/namespace.yaml
kubectl apply -f config/
kubectl apply -f infrastructure/

# Applications only
kubectl apply -f apps/

# Ingress only
kubectl apply -f ingress/
```

### Verify Deployment

```bash
# Check all pods are running
kubectl get pods -n sbcgg

# Check services
kubectl get svc -n sbcgg

# Check ingress
kubectl get ingress -n sbcgg

# Check HPA status
kubectl get hpa -n sbcgg

# View logs
kubectl logs -n sbcgg -l app=gateway --tail=100 -f
```

## Accessing Services

### Local Development

Access services via ingress:

- **GraphQL API**: http://sbcgg.local/graphql
- **GraphiQL**: http://sbcgg.local/graphiql
- **Grafana**: http://grafana.sbcgg.local
- **Consul UI**: http://consul.sbcgg.local
- **Keycloak**: http://keycloak.sbcgg.local

### Port Forwarding (Alternative)

```bash
# Gateway
kubectl port-forward -n sbcgg svc/gateway 8080:8080

# Grafana
kubectl port-forward -n sbcgg svc/lgtm 3000:3000

# Consul
kubectl port-forward -n sbcgg svc/consul 8500:8500

# Keycloak
kubectl port-forward -n sbcgg svc/keycloak 9090:8080
```

## Monitoring & Observability

### Grafana LGTM Stack

Access Grafana at http://grafana.sbcgg.local

The LGTM stack provides:

- **Loki**: Log aggregation
- **Grafana**: Visualization dashboards
- **Tempo**: Distributed tracing
- **Mimir**: Metrics storage

### Viewing Logs

```bash
# All logs from a service
kubectl logs -n sbcgg -l app=users-service --tail=100

# Follow logs
kubectl logs -n sbcgg -l app=gateway -f

# Logs from all pods
kubectl logs -n sbcgg --all-containers=true --tail=50
```

### Metrics

```bash
# Check HPA metrics
kubectl get hpa -n sbcgg

# Detailed HPA status
kubectl describe hpa gateway-hpa -n sbcgg

# Top pods (requires metrics-server)
kubectl top pods -n sbcgg

# Top nodes
kubectl top nodes
```

## Scaling

### Manual Scaling

```bash
# Scale a deployment
kubectl scale deployment gateway -n sbcgg --replicas=5

# Check current replicas
kubectl get deployment gateway -n sbcgg
```

### Horizontal Pod Autoscaler (HPA)

HPA is automatically configured for all microservices:

```yaml
minReplicas: 2
maxReplicas: 10 (20 for gateway)
targetCPUUtilizationPercentage: 70
targetMemoryUtilizationPercentage: 80
```

View HPA status:

```bash
kubectl get hpa -n sbcgg
kubectl describe hpa users-service-hpa -n sbcgg
```

### Vertical Scaling

Adjust resource requests/limits in deployment files:

```yaml
resources:
  requests:
    memory: "512Mi"
    cpu: "250m"
  limits:
    memory: "1Gi"
    cpu: "1000m"
```

## Production Considerations

### 1. Security

- **Change all default passwords** in `config/secrets.yaml`
- Use **Kubernetes Secrets encryption at rest**
- Configure **Network Policies** to restrict pod-to-pod communication
- Enable **RBAC** and create service accounts with minimal permissions
- Use **Pod Security Standards** (restricted profile)
- Implement **secret management** (e.g., HashiCorp Vault, AWS Secrets Manager)

```bash
# Example: Use Sealed Secrets for GitOps
kubectl create secret generic sbcgg-secrets \
  --from-literal=password=changeme \
  --dry-run=client -o yaml | \
  kubeseal -o yaml > sealed-secrets.yaml
```

### 2. High Availability

- **Multi-zone deployment**: Distribute pods across availability zones
- **Pod Disruption Budgets**: Ensure minimum availability during updates
- **PostgreSQL**: Use managed service or configure replication
- **Redis**: Use Redis Sentinel or Redis Cluster
- **Consul**: Run 3+ replicas for HA

Example Pod Disruption Budget:

```yaml
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: gateway-pdb
  namespace: sbcgg
spec:
  minAvailable: 1
  selector:
    matchLabels:
      app: gateway
```

### 3. Storage

- **Use persistent volumes** for stateful services
- Configure **storage classes** appropriate for your cloud provider
- Implement **backup strategies** for databases
- Consider **managed databases** (RDS, Cloud SQL, etc.) for production

### 4. TLS/SSL

- Install **cert-manager** for automatic certificate management
- Use **Let's Encrypt** for free TLS certificates
- Update ingress with TLS configuration

```bash
# Install cert-manager
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.0/cert-manager.yaml

# Create ClusterIssuer
kubectl apply -f - <<EOF
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: your-email@example.com
    privateKeySecretRef:
      name: letsencrypt-prod
    solvers:
    - http01:
        ingress:
          class: nginx
EOF
```

### 5. Observability

- Configure **log retention** policies
- Set up **alerting** in Grafana
- Implement **custom metrics** for business KPIs
- Use **distributed tracing** for debugging

### 6. CI/CD

Integrate with your CI/CD pipeline:

```yaml
# GitLab CI example
deploy:
  stage: deploy
  script:
    - kubectl config use-context production
    - kubectl apply -k infrastructure/kubernetes/
  only:
    - main
```

### 7. Resource Quotas

Set namespace resource quotas:

```yaml
apiVersion: v1
kind: ResourceQuota
metadata:
  name: sbcgg-quota
  namespace: sbcgg
spec:
  hard:
    requests.cpu: "10"
    requests.memory: "20Gi"
    limits.cpu: "20"
    limits.memory: "40Gi"
    pods: "50"
```

## Troubleshooting

### Pods Not Starting

```bash
# Check pod status
kubectl get pods -n sbcgg

# Describe pod for events
kubectl describe pod <pod-name> -n sbcgg

# Check logs
kubectl logs <pod-name> -n sbcgg

# Check init container logs
kubectl logs <pod-name> -n sbcgg -c wait-for-dependencies
```

### Service Connection Issues

```bash
# Test service connectivity from within cluster
kubectl run -it --rm debug --image=busybox --restart=Never -n sbcgg -- sh
# Inside the pod:
nc -zv postgresql.sbcgg.svc.cluster.local 5432
nc -zv consul.sbcgg.svc.cluster.local 8500

# Check service endpoints
kubectl get endpoints -n sbcgg
```

### Ingress Not Working

```bash
# Check ingress status
kubectl get ingress -n sbcgg
kubectl describe ingress sbcgg-ingress -n sbcgg

# Check ingress controller logs
kubectl logs -n ingress-nginx -l app.kubernetes.io/component=controller

# Test ingress from inside cluster
kubectl run -it --rm test --image=curlimages/curl --restart=Never -- \
  curl -H "Host: sbcgg.local" http://gateway.sbcgg.svc.cluster.local:8080/actuator/health
```

### Database Connection Errors

```bash
# Check PostgreSQL pod
kubectl logs -n sbcgg -l app=postgresql

# Connect to PostgreSQL
kubectl exec -it postgresql-0 -n sbcgg -- psql -U postgres -d users

# Check database initialization
kubectl logs -n sbcgg postgresql-0 | grep "database system is ready"
```

### HPA Not Scaling

```bash
# Check metrics server
kubectl get apiservice v1beta1.metrics.k8s.io -o yaml

# Check HPA status
kubectl describe hpa gateway-hpa -n sbcgg

# Check if metrics are available
kubectl top pods -n sbcgg
```

### Consul Configuration Issues

```bash
# Check Consul logs
kubectl logs -n sbcgg -l app=consul

# Verify Consul KV store
kubectl exec -it consul-0 -n sbcgg -- consul kv get config/users-service,kubernetes/data

# Check Consul members
kubectl exec -it consul-0 -n sbcgg -- consul members
```

### Rolling Back Deployment

```bash
# Check deployment history
kubectl rollout history deployment gateway -n sbcgg

# Rollback to previous version
kubectl rollout undo deployment gateway -n sbcgg

# Rollback to specific revision
kubectl rollout undo deployment gateway -n sbcgg --to-revision=2
```

## Cleanup

To remove all resources:

```bash
# Delete everything in the namespace
kubectl delete namespace sbcgg

# Or delete using kustomize
kubectl delete -k .
```

## Additional Resources

- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Kustomize Documentation](https://kustomize.io/)
- [NGINX Ingress Controller](https://kubernetes.github.io/ingress-nginx/)
- [Horizontal Pod Autoscaler](https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale/)
- [Cert-Manager Documentation](https://cert-manager.io/docs/)

## Support

For issues or questions:

- Check the main [CLAUDE.md](../../CLAUDE.md) for development guidelines
- Review [README.md](../../README.md) for project architecture
- Open an issue in the project repository

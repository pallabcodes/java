# Production Deployment Runbook

## Overview

This runbook provides step-by-step instructions for deploying the Payments Platform to production environments. The platform consists of multiple microservices deployed on Kubernetes with supporting infrastructure components.

## Prerequisites

### Required Tools
- kubectl (v1.28+)
- helm (v3.12+)
- terraform (v1.5+)
- awscli (v2.x)
- docker (v24+)
- jq (v1.6+)

### Access Requirements
- AWS account with appropriate IAM permissions
- Kubernetes cluster administrator access
- Docker registry access
- Database administrator access
- Security team approval

### Pre-deployment Checklist
- [ ] Security audit completed and approved
- [ ] Penetration testing results reviewed
- [ ] Compliance checklist signed off
- [ ] Infrastructure capacity verified
- [ ] Backup and recovery tested
- [ ] Rollback plan documented
- [ ] Communication plan prepared

## Phase 1: Infrastructure Provisioning

### Step 1.1: Initialize Terraform
```bash
cd infrastructure/terraform

# Initialize Terraform
terraform init

# Plan infrastructure changes
terraform plan -var-file=environments/prod.tfvars -out=tfplan

# Review the plan
terraform show tfplan

# Apply infrastructure
terraform apply tfplan
```

### Step 1.2: Verify Infrastructure
```bash
# Verify EKS cluster
aws eks update-kubeconfig --region us-east-1 --name payments-platform-prod
kubectl get nodes

# Verify RDS connectivity
kubectl run postgres-test --image=postgres:15-alpine --rm -it \
  --env="PGPASSWORD=$(kubectl get secret postgres-secret -o jsonpath='{.data.password}' | base64 -d)" \
  -- psql -h $(terraform output -raw database_endpoint) -U payments_user -d payments_platform -c "SELECT version();"

# Verify Redis connectivity
kubectl run redis-test --image=redis:7-alpine --rm -it \
  -- redis-cli -h $(terraform output -raw redis_endpoint) -a $(kubectl get secret redis-secret -o jsonpath='{.data.password}' | base64 -d) ping
```

## Phase 2: Kubernetes Base Setup

### Step 2.1: Install Core Components
```bash
# Add Helm repositories
helm repo add istio https://istio-release.storage.googleapis.com/charts
helm repo add cert-manager https://charts.jetstack.io
helm repo add kyverno https://kyverno.github.io/kyverno/
helm repo add kubecost https://kubecost.github.io/cost-analyzer/
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

# Install cert-manager
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.0/cert-manager.yaml

# Install Istio base
helm install istio-base istio/base -n istio-system --create-namespace

# Install Istio discovery
helm install istiod istio/istiod -n istio-system

# Install Istio ingress gateway
helm install istio-ingress istio/gateway -n istio-system
```

### Step 2.2: Install Monitoring Stack
```bash
# Install Prometheus
helm install prometheus prometheus-community/prometheus -n monitoring --create-namespace \
  --set server.persistentVolume.enabled=true \
  --set server.persistentVolume.size=50Gi

# Install Grafana
helm install grafana stable/grafana -n monitoring \
  --set adminPassword='admin_password_strong_123' \
  --set persistence.enabled=true \
  --set persistence.size=10Gi

# Install KubeCost
kubectl create namespace kubecost
kubectl apply -f https://raw.githubusercontent.com/kubecost/cost-analyzer-helm-chart/develop/kubecost/cost-analyzer.yaml

# Install Kyverno
helm install kyverno kyverno/kyverno -n kyverno --create-namespace \
  --set replicaCount=3
```

### Step 2.3: Configure Security Policies
```bash
# Apply Kyverno policies
kubectl apply -f helm/payments-platform/templates/kyverno-policies.yaml

# Apply network policies
kubectl apply -f helm/payments-platform/templates/network-policy.yaml

# Configure Istio security
kubectl apply -f helm/payments-platform/templates/istio-gateway.yaml
```

## Phase 3: Database Setup

### Step 3.1: Initialize Database Schema
```bash
# Get database connection details
DB_HOST=$(terraform output -raw database_endpoint | cut -d: -f1)
DB_PASSWORD=$(kubectl get secret postgres-secret -o jsonpath='{.data.password}' | base64 -d)

# Run database migrations
kubectl run flyway-migration --image=flyway/flyway:9-alpine \
  --env="FLYWAY_URL=jdbc:postgresql://$DB_HOST:5432/payments_platform" \
  --env="FLYWAY_USER=payments_user" \
  --env="FLYWAY_PASSWORD=$DB_PASSWORD" \
  --env="FLYWAY_LOCATIONS=filesystem:/migrations" \
  --overrides='{"spec":{"volumes":[{"name":"migrations","configMap":{"name":"flyway-migrations"}}],"containers":[{"volumeMounts":[{"name":"migrations","mountPath":"/migrations"}]}]}}' \
  --rm --restart=Never -- flyway migrate
```

### Step 3.2: Load Reference Data
```bash
# Load initial data
kubectl run data-loader --image=payments/data-loader:latest \
  --env="DB_HOST=$DB_HOST" \
  --env="DB_PASSWORD=$DB_PASSWORD" \
  --rm --restart=Never
```

## Phase 4: Application Deployment

### Step 4.1: Build and Push Images
```bash
# Build all services
cd KotlinPaymentsPlatform

# Build API Gateway
docker build -t payments/api-gateway:$(git rev-parse --short HEAD) api-gateway/
docker push payments/api-gateway:$(git rev-parse --short HEAD)

# Build Payments Service
docker build -t payments/payments-service:$(git rev-parse --short HEAD) payments-service/
docker push payments/payments-service:$(git rev-parse --short HEAD)

# Build Risk Service
docker build -t payments/risk-service:$(git rev-parse --short HEAD) risk-service/
docker push payments/risk-service:$(git rev-parse --short HEAD)

# Build Ledger Service
docker build -t payments/ledger-service:$(git rev-parse --short HEAD) ledger-service/
docker push payments/ledger-service:$(git rev-parse --short HEAD)
```

### Step 4.2: Deploy with Helm
```bash
# Create namespace
kubectl create namespace payments-platform

# Deploy payments platform
helm install payments-platform ./helm/payments-platform \
  -n payments-platform \
  --set image.tag=$(git rev-parse --short HEAD) \
  --set global.env=production \
  --values helm/payments-platform/values-prod.yaml
```

### Step 4.3: Verify Deployment
```bash
# Check pod status
kubectl get pods -n payments-platform

# Check service endpoints
kubectl get svc -n payments-platform

# Verify Istio configuration
kubectl get virtualservice -n payments-platform

# Check Kyverno policies
kubectl get clusterpolicy

# Test application health
curl -k https://payments.yourdomain.com/actuator/health
```

## Phase 5: ArgoCD Setup

### Step 5.1: Install ArgoCD
```bash
# Install ArgoCD
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# Wait for ArgoCD to be ready
kubectl wait --for=condition=available --timeout=300s deployment/argocd-server -n argocd
```

### Step 5.2: Configure ArgoCD Applications
```bash
# Apply application manifests
kubectl apply -f argocd/applications/payments-platform.yaml

# Get ArgoCD admin password
ARGOCD_PASSWORD=$(kubectl get secret argocd-initial-admin-secret -n argocd -o jsonpath='{.data.password}' | base64 -d)

# Login to ArgoCD
argocd login argocd.yourdomain.com --username admin --password $ARGOCD_PASSWORD

# Sync applications
argocd app sync payments-platform
argocd app sync payments-platform-monitoring
argocd app sync payments-platform-databases
```

### Step 5.3: Enable Auto-Sync
```bash
# Configure auto-sync for production
argocd app set payments-platform --sync-policy automated
argocd app set payments-platform --auto-prune
argocd app set payments-platform --self-heal
```

## Phase 6: Traffic Migration

### Step 6.1: Blue-Green Deployment
```bash
# Create blue environment (current production)
kubectl label namespace payments-platform environment=blue

# Deploy green environment
kubectl create namespace payments-platform-green
helm install payments-platform-green ./helm/payments-platform \
  -n payments-platform-green \
  --set image.tag=$(git rev-parse --short HEAD)

# Test green environment
kubectl port-forward -n payments-platform-green svc/payments-platform-api-gateway 8080:80
curl http://localhost:8080/actuator/health
```

### Step 6.2: Traffic Switching
```bash
# Update Istio VirtualService to route to green
kubectl apply -f - <<EOF
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: payments-platform-traffic-switch
  namespace: payments-platform
spec:
  hosts:
  - payments.yourdomain.com
  http:
  - route:
    - destination:
        host: payments-platform-green
      weight: 100
    - destination:
        host: payments-platform-blue
      weight: 0
EOF
```

### Step 6.3: Verify Traffic Migration
```bash
# Monitor traffic distribution
kubectl logs -n istio-system deployment/istio-ingressgateway | grep payments.yourdomain.com

# Check application metrics
kubectl port-forward -n payments-platform-green svc/payments-platform-prometheus 9090:9090
curl http://localhost:9090/api/v1/query?query=up
```

### Step 6.4: Complete Migration
```bash
# Remove blue environment
kubectl delete namespace payments-platform

# Rename green to production
kubectl label namespace payments-platform-green environment=production
kubectl annotate namespace payments-platform-green description="Production payments platform"

# Update DNS if needed
aws route53 change-resource-record-sets \
  --hosted-zone-id Z123456789 \
  --change-batch file://dns-update.json
```

## Phase 7: Post-Deployment Verification

### Step 7.1: Health Checks
```bash
# Application health
curl -k https://payments.yourdomain.com/actuator/health

# Service mesh health
kubectl get pods -n istio-system

# Monitoring stack health
kubectl get pods -n monitoring
```

### Step 7.2: Performance Testing
```bash
# Run load tests
cd load-testing
k6 run --env BASE_URL=https://payments.yourdomain.com load-test.js

# Check performance metrics
kubectl port-forward -n monitoring svc/prometheus 9090:9090
curl "http://localhost:9090/api/v1/query?query=http_request_duration_seconds{quantile=\"0.95\"}"
```

### Step 7.3: Security Verification
```bash
# Run security audit
cd security-audit
./audit.sh

# Verify SSL certificates
openssl s_client -connect payments.yourdomain.com:443 -servername payments.yourdomain.com

# Check security headers
curl -I https://payments.yourdomain.com
```

## Phase 8: Monitoring and Alerting Setup

### Step 8.1: Configure Grafana
```bash
# Get Grafana admin password
GRAFANA_PASSWORD=$(kubectl get secret grafana -n monitoring -o jsonpath='{.data.admin-password}' | base64 -d)

# Import dashboards
kubectl apply -f monitoring/grafana-dashboard.json

# Configure alert rules
kubectl apply -f monitoring/alert_rules.yml
```

### Step 8.2: Setup Alert Channels
```bash
# Configure AlertManager
kubectl apply -f - <<EOF
apiVersion: v1
kind: ConfigMap
metadata:
  name: alertmanager-config
  namespace: monitoring
data:
  config.yml: |
    global:
      smtp_smarthost: 'smtp.gmail.com:587'
      smtp_from: 'alerts@yourdomain.com'
    route:
      group_by: ['alertname']
      group_wait: 10s
      group_interval: 10s
      repeat_interval: 1h
      receiver: 'email'
    receivers:
    - name: 'email'
      email_configs:
      - to: 'team@yourdomain.com'
EOF
```

### Step 8.3: Cost Monitoring Setup
```bash
# Configure KubeCost
kubectl port-forward -n kubecost svc/kubecost-cost-analyzer 9090:9090

# Set up cost alerts
curl -X POST http://localhost:9090/model/costDataModel \
  -H "Content-Type: application/json" \
  -d '{"window": "7d", "aggregate": "namespace"}'
```

## Phase 9: Backup and Disaster Recovery

### Step 9.1: Configure Backups
```bash
# Database backup job
kubectl apply -f - <<EOF
apiVersion: batch/v1
kind: CronJob
metadata:
  name: database-backup
  namespace: payments-platform
spec:
  schedule: "0 */6 * * *"
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: backup
            image: postgres:15-alpine
            command: ["/bin/bash", "-c"]
            args:
            - |
              pg_dump -h $DB_HOST -U $DB_USER payments_platform | gzip > /backup/backup_$(date +%Y%m%d_%H%M%S).sql.gz
            env:
            - name: DB_HOST
              valueFrom:
                secretKeyRef:
                  name: postgres-secret
                  key: host
            - name: DB_USER
              valueFrom:
                secretKeyRef:
                  name: postgres-secret
                  key: username
            volumeMounts:
            - name: backup-volume
              mountPath: /backup
          volumes:
          - name: backup-volume
            persistentVolumeClaim:
              claimName: backup-pvc
          restartPolicy: OnFailure
EOF
```

### Step 9.2: Disaster Recovery Testing
```bash
# Test failover procedures
kubectl apply -f chaos-engineering/chaos-experiments.yaml

# Verify backup restoration
./backup-restore-test.sh

# Test cross-region failover
aws rds failover-db-cluster --db-cluster-identifier payments-platform-prod
```

## Phase 10: Go-Live Checklist

### Pre-Go-Live Verification
- [ ] All services deployed and healthy
- [ ] Load testing completed successfully
- [ ] Security audit passed
- [ ] Monitoring and alerting configured
- [ ] Backup and recovery tested
- [ ] Rollback procedures documented
- [ ] Support team trained
- [ ] Customer communication prepared

### Go-Live Execution
1. **Traffic Switch**: Update DNS/load balancer
2. **Monitor Closely**: Watch metrics and alerts for 2 hours
3. **Customer Communication**: Notify users of successful launch
4. **Post-Mortem**: Review deployment and identify improvements

### Post-Go-Live Monitoring
- [ ] Monitor error rates and performance
- [ ] Watch resource utilization
- [ ] Review security events
- [ ] Collect user feedback
- [ ] Plan for scaling and optimization

## Emergency Procedures

### Service Outage Response
1. **Assess Impact**: Determine affected services and users
2. **Check Monitoring**: Review dashboards and alerts
3. **Implement Workaround**: Route traffic to backup systems
4. **Deploy Fix**: Apply emergency patches
5. **Verify Recovery**: Test all critical functions
6. **Communicate**: Update stakeholders on status

### Rollback Procedure
```bash
# Emergency rollback
kubectl rollout undo deployment/payments-platform-api-gateway -n payments-platform
kubectl rollout undo deployment/payments-platform-payments-service -n payments-platform
kubectl rollout undo deployment/payments-platform-risk-service -n payments-platform
kubectl rollout undo deployment/payments-platform-ledger-service -n payments-platform

# Verify rollback
kubectl get pods -n payments-platform
curl https://payments.yourdomain.com/actuator/health
```

## Support and Maintenance

### Regular Maintenance Tasks
- **Daily**: Monitor dashboards and alerts
- **Weekly**: Review security scans and updates
- **Monthly**: Performance testing and optimization
- **Quarterly**: Security audits and compliance reviews
- **Annually**: Disaster recovery testing

### Contact Information
- **Platform Team**: platform@company.com
- **Security Team**: security@company.com
- **Infrastructure Team**: infra@company.com
- **Emergency Hotline**: +1-800-PLATFORM

---

**This runbook ensures reliable, secure, and scalable production deployments of the Payments Platform.**

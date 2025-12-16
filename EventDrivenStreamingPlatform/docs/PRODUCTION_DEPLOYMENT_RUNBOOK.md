# Production Deployment Runbook

## Overview

This runbook provides step-by-step procedures for deploying the Event-Driven Streaming Platform to production environments. All deployments follow a canary deployment strategy with automated rollback capabilities.

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Pre-Deployment Checklist](#pre-deployment-checklist)
3. [Deployment Procedures](#deployment-procedures)
4. [Post-Deployment Validation](#post-deployment-validation)
5. [Rollback Procedures](#rollback-procedures)
6. [Troubleshooting](#troubleshooting)
7. [Emergency Procedures](#emergency-procedures)

## Prerequisites

### Required Access
- Kubernetes cluster admin access
- Helm CLI installed
- kubectl configured for production cluster
- GitHub repository access
- Container registry access

### Required Secrets
```bash
# Check that all required secrets exist
kubectl get secrets -n streaming-platform
```

Expected secrets:
- `database-secret`: Database credentials
- `redis-secret`: Redis credentials
- `kafka-secret`: Kafka configuration
- `jwt-secret`: JWT signing keys
- `external-apis-secret`: External API keys

### Environment Validation
```bash
# Validate cluster connectivity
kubectl cluster-info

# Check node status
kubectl get nodes

# Verify storage classes
kubectl get storageclass

# Check ingress controller
kubectl get pods -n ingress-nginx
```

## Pre-Deployment Checklist

### 🔍 Code Quality Checks
- [ ] All tests passing (unit, integration, contract)
- [ ] Security scan passed (no critical vulnerabilities)
- [ ] Code coverage > 80%
- [ ] SonarQube quality gate passed
- [ ] Dependency check passed

### 🏗️ Build Validation
- [ ] Docker images built successfully
- [ ] Multi-platform images (amd64, arm64)
- [ ] SBOM generated and validated
- [ ] Image security scan passed
- [ ] Images pushed to registry

### 📊 Performance Validation
- [ ] Load testing completed successfully
- [ ] Response time < 100ms (95th percentile)
- [ ] Error rate < 0.1%
- [ ] Memory/CPU within limits

### 🤝 Contract Testing
- [ ] All microservice contracts validated
- [ ] Pact contracts published and verified
- [ ] API compatibility confirmed

### 💥 Chaos Engineering
- [ ] Chaos experiments executed successfully
- [ ] System resilience validated
- [ ] Failure recovery tested

## Deployment Procedures

### Automated Deployment (Recommended)

Use the GitHub Actions production deployment pipeline:

1. **Create Release**
   ```bash
   git tag v1.2.3
   git push origin v1.2.3
   ```

2. **Monitor Deployment**
   - Go to GitHub Actions tab
   - Monitor "Production Deployment Pipeline"
   - Check each job status

3. **Validate Deployment**
   - Check Grafana dashboards
   - Monitor Prometheus alerts
   - Review deployment report

### Manual Deployment (Fallback)

If automated deployment fails:

#### Step 1: Prepare Environment
```bash
# Set deployment variables
export RELEASE_VERSION=v1.2.3
export NAMESPACE=streaming-platform
export IMAGE_TAG=v1.2.3

# Configure kubectl
kubectl config use-context production-cluster
```

#### Step 2: Backup Current State
```bash
# Create backup of current Helm release
helm get values streaming-platform -n $NAMESPACE > backup-values.yaml

# Backup current images
kubectl get deployments -n $NAMESPACE -o yaml > backup-deployments.yaml
```

#### Step 3: Deploy Infrastructure Components
```bash
# Deploy PostgreSQL
helm upgrade --install streaming-platform-postgresql bitnami/postgresql \
  --namespace $NAMESPACE \
  --values helm/streaming-platform/values-production.yaml \
  --set postgresql.auth.postgresPassword=<password> \
  --wait

# Deploy Redis
helm upgrade --install streaming-platform-redis bitnami/redis \
  --namespace $NAMESPACE \
  --values helm/streaming-platform/values-production.yaml \
  --wait

# Deploy Kafka
helm upgrade --install streaming-platform-kafka bitnami/kafka \
  --namespace $NAMESPACE \
  --values helm/streaming-platform/values-production.yaml \
  --wait
```

#### Step 4: Deploy Monitoring Stack
```bash
# Deploy Prometheus
helm upgrade --install streaming-platform-prometheus prometheus-community/prometheus \
  --namespace $NAMESPACE \
  --values monitoring/production/prometheus-values.yaml \
  --wait

# Deploy Grafana
helm upgrade --install streaming-platform-grafana grafana/grafana \
  --namespace $NAMESPACE \
  --values monitoring/production/grafana-values.yaml \
  --wait
```

#### Step 5: Deploy Application Services
```bash
# Deploy Infrastructure Service
helm upgrade --install streaming-platform-infrastructure ./helm/streaming-platform \
  --namespace $NAMESPACE \
  --set infrastructure.enabled=true \
  --set infrastructure.image.tag=$IMAGE_TAG \
  --values helm/streaming-platform/values-production.yaml \
  --wait

# Deploy Analytics Service
helm upgrade --install streaming-platform-analytics ./helm/streaming-platform \
  --namespace $NAMESPACE \
  --set analytics.enabled=true \
  --set analytics.image.tag=$IMAGE_TAG \
  --values helm/streaming-platform/values-production.yaml \
  --wait

# Deploy Playback Service
helm upgrade --install streaming-platform-playback ./helm/streaming-platform \
  --namespace $NAMESPACE \
  --set playback.enabled=true \
  --set playback.image.tag=$IMAGE_TAG \
  --values helm/streaming-platform/values-production.yaml \
  --wait

# Deploy ML Pipeline Service
helm upgrade --install streaming-platform-ml-pipeline ./helm/streaming-platform \
  --namespace $NAMESPACE \
  --set mlPipeline.enabled=true \
  --set mlPipeline.image.tag=$IMAGE_TAG \
  --values helm/streaming-platform/values-production.yaml \
  --wait
```

#### Step 6: Configure Traffic Management
```bash
# Deploy Istio Gateway
kubectl apply -f helm/streaming-platform/templates/istio-gateway.yaml

# Configure Virtual Services
kubectl apply -f helm/streaming-platform/templates/virtual-services.yaml
```

## Post-Deployment Validation

### Health Checks
```bash
# Check all pods are running
kubectl get pods -n streaming-platform

# Check service health endpoints
kubectl exec -n streaming-platform deployment/streaming-platform-infrastructure -- \
  curl -f http://localhost:8081/actuator/health

# Check ingress
curl -f https://api.streaming-platform.company.com/actuator/health
```

### Functional Validation
```bash
# Test basic API endpoints
curl -f https://api.streaming-platform.company.com/v1/health

# Test service-to-service communication
kubectl exec -n streaming-platform deployment/streaming-platform-analytics -- \
  curl -f http://streaming-platform-infrastructure:8081/actuator/health
```

### Performance Validation
```bash
# Check response times in Grafana
# Validate error rates are below threshold
# Confirm monitoring metrics are being collected
```

### Traffic Validation
```bash
# Check that traffic is being routed correctly
kubectl get virtualservices -n streaming-platform

# Validate that all services receive traffic
kubectl logs -n streaming-platform deployment/streaming-platform-infrastructure --tail=100
```

## Rollback Procedures

### Automated Rollback
```bash
# Rollback to previous Helm release
helm rollback streaming-platform 1 -n streaming-platform

# Or rollback specific service
helm rollback streaming-platform-infrastructure 1 -n streaming-platform
```

### Manual Rollback
```bash
# Scale down new deployment
kubectl scale deployment streaming-platform-infrastructure-v1-2-3 --replicas=0 -n streaming-platform

# Scale up previous deployment
kubectl scale deployment streaming-platform-infrastructure-v1-2-2 --replicas=3 -n streaming-platform

# Update VirtualService to route to previous version
kubectl patch virtualservice streaming-platform -n streaming-platform \
  --type json -p='[{"op": "replace", "path": "/spec/http/0/route/0/destination/subset", "value": "v1.2.2"}]'
```

### Emergency Rollback
If immediate rollback is needed:
```bash
# Immediate traffic shift to stable version
kubectl patch virtualservice streaming-platform -n streaming-platform \
  --type json -p='[{"op": "replace", "path": "/spec/http/0/route/0/weight", "value": 0},{"op": "replace", "path": "/spec/http/0/route/1/weight", "value": 100}]'
```

## Troubleshooting

### Common Issues

#### Pods Not Starting
```bash
# Check pod status
kubectl describe pod <pod-name> -n streaming-platform

# Check pod logs
kubectl logs <pod-name> -n streaming-platform

# Check events
kubectl get events -n streaming-platform --sort-by=.metadata.creationTimestamp
```

#### Service Unavailable
```bash
# Check service endpoints
kubectl get endpoints -n streaming-platform

# Check service configuration
kubectl describe service <service-name> -n streaming-platform

# Test service connectivity
kubectl exec -n streaming-platform deployment/streaming-platform-infrastructure -- \
  curl -v http://<service-name>:8080/actuator/health
```

#### High Error Rates
```bash
# Check application logs
kubectl logs -n streaming-platform deployment/streaming-platform-infrastructure --tail=100

# Check metrics in Prometheus
# Look for error patterns in Grafana

# Check circuit breaker status
kubectl exec -n streaming-platform deployment/streaming-platform-infrastructure -- \
  curl http://localhost:8081/actuator/circuitbreakers
```

#### Database Connection Issues
```bash
# Check database pod status
kubectl get pods -n streaming-platform -l app.kubernetes.io/name=postgresql

# Check database logs
kubectl logs -n streaming-platform -l app.kubernetes.io/name=postgresql

# Test database connectivity
kubectl exec -n streaming-platform deployment/streaming-platform-infrastructure -- \
  nc -z streaming-platform-postgresql 5432
```

### Performance Issues

#### High CPU Usage
```bash
# Check CPU usage
kubectl top pods -n streaming-platform

# Check JVM thread dumps
kubectl exec -n streaming-platform deployment/streaming-platform-infrastructure -- \
  jcmd 1 Thread.print > thread-dump.txt

# Scale up if needed
kubectl scale deployment streaming-platform-infrastructure --replicas=5 -n streaming-platform
```

#### High Memory Usage
```bash
# Check memory usage
kubectl top pods -n streaming-platform

# Check JVM heap usage
kubectl exec -n streaming-platform deployment/streaming-platform-infrastructure -- \
  jcmd 1 GC.heap_info

# Check for memory leaks
kubectl exec -n streaming-platform deployment/streaming-platform-infrastructure -- \
  jcmd 1 GC.heap_dump heap-dump.hprof
```

#### Slow Response Times
```bash
# Check application metrics
kubectl exec -n streaming-platform deployment/streaming-platform-infrastructure -- \
  curl http://localhost:8081/actuator/metrics

# Check database query performance
kubectl exec -n streaming-platform -l app.kubernetes.io/name=postgresql -- \
  psql -c "SELECT * FROM pg_stat_activity WHERE state = 'active';"

# Check network latency
kubectl exec -n streaming-platform deployment/streaming-platform-infrastructure -- \
  traceroute streaming-platform-postgresql
```

## Emergency Procedures

### Complete System Shutdown
```bash
# Scale down all services
kubectl scale deployment --all --replicas=0 -n streaming-platform

# Stop ingress traffic
kubectl patch ingress streaming-platform-ingress -n streaming-platform \
  -p '{"spec":{"ingressClassName":"nginx-emergency"}}'

# Notify stakeholders
# Send emergency notifications
```

### Database Emergency
```bash
# Create database backup
kubectl exec -n streaming-platform -l app.kubernetes.io/name=postgresql -- \
  pg_dumpall -U postgres > emergency-backup.sql

# Scale down application services
kubectl scale deployment --all --replicas=0 -n streaming-platform

# Perform database maintenance
# Restart database if needed
kubectl delete pod -l app.kubernetes.io/name=postgresql -n streaming-platform
```

### Network Emergency
```bash
# Check network policies
kubectl get networkpolicies -n streaming-platform

# Reset Istio configuration
kubectl delete virtualservice --all -n streaming-platform
kubectl delete destinationrule --all -n streaming-platform

# Restart Istio ingress gateway
kubectl delete pod -l app=istio-ingressgateway -n istio-system
```

## Monitoring During Deployment

### Key Metrics to Monitor
- **Response Time**: < 100ms (95th percentile)
- **Error Rate**: < 0.1%
- **CPU Usage**: < 80%
- **Memory Usage**: < 85%
- **Database Connections**: < 80% of max
- **Kafka Consumer Lag**: < 10,000 messages

### Alert Thresholds
- Response time > 200ms for 5 minutes
- Error rate > 1% for 5 minutes
- CPU usage > 90% for 10 minutes
- Memory usage > 95% for 5 minutes
- Database connections > 90% for 5 minutes

### Grafana Dashboards
- Production Overview Dashboard
- Service Health Dashboard
- Performance Metrics Dashboard
- Error Analysis Dashboard

## Post-Deployment Tasks

### Immediate (First 24 hours)
- [ ] Monitor system performance
- [ ] Validate all monitoring alerts
- [ ] Run additional load tests
- [ ] Update documentation

### Short-term (First week)
- [ ] Optimize resource allocations
- [ ] Tune JVM parameters
- [ ] Update baseline metrics
- [ ] Train team on new features

### Long-term (Ongoing)
- [ ] Regular security updates
- [ ] Performance monitoring
- [ ] Capacity planning
- [ ] Disaster recovery testing

---

## Contact Information

### On-Call Engineers
- Primary: @platform-team-lead
- Secondary: @devops-team-lead
- Backup: @infrastructure-team

### Communication Channels
- Slack: #platform-deployments
- Email: platform-team@company.com
- PagerDuty: Platform Services

### Documentation Links
- Architecture: https://docs.company.com/platform/architecture
- API Docs: https://docs.company.com/platform/api
- Monitoring: https://grafana.company.com/d/platform-overview
- Runbooks: https://docs.company.com/platform/runbooks

---

*Last Updated: December 16, 2025*
*Version: 1.0.0*

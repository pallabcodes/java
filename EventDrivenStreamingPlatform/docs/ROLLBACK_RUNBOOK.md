# Rollback Runbook

## Overview

This runbook provides procedures for rolling back deployments of the Event-Driven Streaming Platform. Rollbacks can be triggered automatically by monitoring alerts or manually by operators.

## Table of Contents
1. [Rollback Triggers](#rollback-triggers)
2. [Automated Rollback](#automated-rollback)
3. [Manual Rollback](#manual-rollback)
4. [Emergency Rollback](#emergency-rollback)
5. [Rollback Validation](#rollback-validation)
6. [Post-Rollback Procedures](#post-rollback-procedures)

## Rollback Triggers

### Automatic Triggers
- Error rate > 5% for 5 minutes
- Response time > 500ms (95th percentile) for 5 minutes
- Service unavailable for 3 minutes
- Database connection failures > 10 per minute
- Circuit breaker open state

### Manual Triggers
- Business logic errors discovered
- Security vulnerabilities identified
- Performance degradation observed
- Data corruption detected
- External dependency failures

## Automated Rollback

### GitHub Actions Automated Rollback

The CI/CD pipeline automatically triggers rollback on critical failures:

```yaml
# In .github/workflows/production-deployment.yml
- name: Monitor post-deployment
  run: |
    # Monitor error rates, latency, etc. for 10 minutes
    sleep 600

    # If issues detected, rollback automatically
    if [ $(kubectl get pods -n streaming-platform -l color=$NEW_COLOR --no-headers | grep -v Running | wc -l) -gt 0 ]; then
      echo "Issues detected, initiating rollback..."
      # Rollback logic
    fi
```

### Prometheus Alert-Triggered Rollback

Critical alerts automatically trigger rollback webhooks:

```yaml
# Alert manager configuration
routes:
- match:
    severity: critical
  receiver: 'rollback-webhook'
  group_wait: 30s
  group_interval: 5m
  repeat_interval: 1h

receivers:
- name: 'rollback-webhook'
  webhook_configs:
  - url: 'http://rollback-service.streaming-platform.svc.cluster.local/rollback'
    http_config:
      bearer_token: '${ROLLBACK_TOKEN}'
```

## Manual Rollback

### Helm-Based Rollback

#### Single Service Rollback
```bash
# Rollback specific service to previous version
helm rollback streaming-platform-infrastructure 1 -n streaming-platform

# Rollback to specific revision
helm rollback streaming-platform-analytics 2 -n streaming-platform
```

#### Full Platform Rollback
```bash
# Rollback all services
helm rollback streaming-platform 1 -n streaming-platform

# Or rollback each service individually
for service in infrastructure analytics playback ml-pipeline; do
  helm rollback streaming-platform-$service 1 -n streaming-platform
done
```

### Kubernetes Manifest Rollback

#### Using Deployment History
```bash
# Check deployment history
kubectl rollout history deployment/streaming-platform-infrastructure -n streaming-platform

# Rollback to previous revision
kubectl rollout undo deployment/streaming-platform-infrastructure -n streaming-platform

# Rollback to specific revision
kubectl rollout undo deployment/streaming-platform-infrastructure --to-revision=2 -n streaming-platform
```

#### Manual Manifest Application
```bash
# Apply previous version manifests
kubectl apply -f kubernetes/production/previous-version/

# Update ConfigMaps and Secrets if needed
kubectl apply -f kubernetes/production/previous-version/config/
```

### Traffic Management Rollback

#### Istio VirtualService Rollback
```bash
# Shift traffic back to stable version
kubectl patch virtualservice streaming-platform -n streaming-platform \
  --type json -p='[
    {"op": "replace", "path": "/spec/http/0/route/0/weight", "value": 0},
    {"op": "replace", "path": "/spec/http/0/route/1/weight", "value": 100}
  ]'
```

#### Ingress Rollback
```bash
# Update ingress to point to previous service
kubectl patch ingress streaming-platform-ingress -n streaming-platform \
  --type json -p='[
    {"op": "replace", "path": "/spec/rules/0/http/paths/0/backend/service/name", "value": "streaming-platform-stable"}
  ]'
```

## Emergency Rollback

### Immediate Traffic Cutover
```bash
# Emergency traffic shift - 100% to stable version
kubectl patch virtualservice streaming-platform -n streaming-platform \
  --type json -p='[
    {"op": "replace", "path": "/spec/http/0/route/0/destination/subset", "value": "stable"},
    {"op": "replace", "path": "/spec/http/0/route/0/weight", "value": 0},
    {"op": "replace", "path": "/spec/http/0/route/1/weight", "value": 100}
  ]'
```

### Service Shutdown
```bash
# Scale down problematic deployment
kubectl scale deployment streaming-platform-infrastructure-v1-2-3 --replicas=0 -n streaming-platform

# Scale up stable deployment
kubectl scale deployment streaming-platform-infrastructure-stable --replicas=3 -n streaming-platform
```

### Database Rollback (if needed)
```bash
# If database changes need rollback
kubectl exec -n streaming-platform -l app.kubernetes.io/name=postgresql -- \
  psql -U postgres -d streaming_platform -f /path/to/rollback-script.sql
```

### Cache Invalidation
```bash
# Clear Redis cache if needed
kubectl exec -n streaming-platform -l app.kubernetes.io/name=redis -- \
  redis-cli FLUSHALL
```

## Rollback Validation

### Health Checks
```bash
# Check all services are healthy
kubectl get pods -n streaming-platform

# Verify service endpoints
for service in infrastructure analytics playback ml-pipeline; do
  kubectl exec -n streaming-platform deployment/streaming-platform-$service -- \
    curl -f http://localhost:8081/actuator/health
done
```

### Functional Validation
```bash
# Test critical user journeys
curl -f https://api.streaming-platform.company.com/v1/health
curl -f https://api.streaming-platform.company.com/v1/playback/start

# Validate service-to-service communication
kubectl exec -n streaming-platform deployment/streaming-platform-analytics -- \
  curl -f http://streaming-platform-infrastructure:8081/actuator/health
```

### Performance Validation
```bash
# Check metrics return to normal
# Error rate < 0.1%
# Response time < 100ms (95th percentile)
# CPU/Memory usage within limits
```

### Data Consistency
```bash
# Verify database consistency
kubectl exec -n streaming-platform -l app.kubernetes.io/name=postgresql -- \
  psql -U postgres -d streaming_platform -c "SELECT count(*) FROM event_store;"

# Check message queues
kubectl exec -n streaming-platform -l app.kubernetes.io/name=kafka -- \
  kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group analytics-service --describe
```

## Post-Rollback Procedures

### Immediate Actions (First 30 minutes)
- [ ] Verify system stability
- [ ] Confirm traffic is flowing correctly
- [ ] Check monitoring dashboards
- [ ] Notify stakeholders of rollback

### Root Cause Analysis (Next 2 hours)
- [ ] Review deployment logs
- [ ] Analyze monitoring metrics
- [ ] Check application logs for errors
- [ ] Identify root cause of failure

### Remediation Planning (Next 4 hours)
- [ ] Fix identified issues in code
- [ ] Update deployment procedures if needed
- [ ] Plan next deployment attempt
- [ ] Update incident documentation

### Follow-up Actions (Next 24 hours)
- [ ] Schedule new deployment with fixes
- [ ] Update deployment runbooks
- [ ] Review monitoring alerts
- [ ] Update team on findings

## Rollback Monitoring

### Key Metrics to Monitor
- **Traffic Distribution**: Ensure all traffic goes to stable version
- **Error Rate**: Should return to baseline levels
- **Response Time**: Should improve to normal levels
- **Resource Usage**: Should stabilize
- **Service Health**: All services should be healthy

### Alert Suppression
```bash
# Temporarily suppress alerts during rollback
kubectl patch prometheusrule streaming-platform-alerts -n monitoring \
  --type json -p='[{"op": "replace", "path": "/spec/groups/0/rules/0/labels/severity", "value": "info"}]'
```

### Monitoring Dashboards
- Rollback Progress Dashboard
- Traffic Distribution Dashboard
- Error Rate Recovery Dashboard
- Performance Recovery Dashboard

## Rollback Scenarios

### Scenario 1: Service Crash Loop
**Symptoms**: Pods restarting continuously
**Rollback**: Immediate scale down of new deployment, scale up stable
**Validation**: Check pod logs, verify stable version stability

### Scenario 2: High Error Rate
**Symptoms**: Error rate spikes after deployment
**Rollback**: Gradual traffic shift back to stable version
**Validation**: Monitor error rate return to baseline

### Scenario 3: Performance Degradation
**Symptoms**: Response times increase significantly
**Rollback**: Traffic shift based on performance metrics
**Validation**: Performance returns to acceptable levels

### Scenario 4: Data Corruption
**Symptoms**: Invalid data or business logic errors
**Rollback**: Full rollback + potential data restoration
**Validation**: Data integrity checks, business logic validation

### Scenario 5: External Dependency Failure
**Symptoms**: Failures due to external service changes
**Rollback**: Rollback until external dependency is resolved
**Validation**: External service health checks

## Rollback Automation

### Rollback Webhook Handler
```java
@PostMapping("/rollback")
public ResponseEntity<Void> handleRollback(@RequestBody RollbackRequest request) {
    logger.warn("Received rollback request: {}", request);

    // Validate rollback request
    if (!isValidRollbackRequest(request)) {
        return ResponseEntity.badRequest().build();
    }

    // Execute rollback
    rollbackService.executeRollback(request.getService(), request.getReason());

    return ResponseEntity.accepted().build();
}
```

### Automated Rollback Service
```java
@Service
public class AutomatedRollbackService {

    public void executeRollback(String service, String reason) {
        // Log rollback initiation
        auditService.logRollback(service, reason);

        // Execute rollback steps
        helmService.rollback(service);
        trafficService.shiftToStable(service);
        monitoringService.suppressAlerts(service);

        // Notify stakeholders
        notificationService.sendRollbackNotification(service, reason);
    }
}
```

## Rollback Testing

### Pre-Deployment Rollback Testing
```bash
# Test rollback procedures in staging
helm install test-release ./helm/streaming-platform -n test
# Deploy new version
helm upgrade test-release ./helm/streaming-platform -n test
# Test rollback
helm rollback test-release 1 -n test
```

### Rollback Validation Scripts
```bash
#!/bin/bash
# rollback-validation.sh

echo "Starting rollback validation..."

# Check service health
health_check() {
  kubectl exec -n streaming-platform deployment/$1 -- curl -f http://localhost:8081/actuator/health
}

# Validate all services
services=("streaming-platform-infrastructure" "streaming-platform-analytics" "streaming-platform-playback")
for service in "${services[@]}"; do
  if health_check "$service"; then
    echo "✅ $service is healthy"
  else
    echo "❌ $service failed health check"
    exit 1
  fi
done

echo "Rollback validation completed successfully"
```

## Documentation Updates

### Post-Rollback Documentation
- [ ] Document root cause findings
- [ ] Update incident response procedures
- [ ] Update deployment runbooks
- [ ] Update monitoring alert thresholds
- [ ] Update rollback procedures

### Lessons Learned
- [ ] What caused the deployment failure?
- [ ] How can we prevent similar issues?
- [ ] What improvements to monitoring/alerting?
- [ ] What updates to deployment procedures?

---

## Contact Information

### Emergency Contacts
- **Platform Lead**: @platform-team-lead (PagerDuty: +1-555-PLATFORM)
- **DevOps Lead**: @devops-team-lead (PagerDuty: +1-555-DEVOPS)
- **On-Call Engineer**: Current on-call rotation

### Communication Channels
- **Slack**: #platform-incidents
- **PagerDuty**: Platform Services escalation
- **Email**: platform-emergency@company.com

### Documentation Links
- **Deployment Runbook**: https://docs.company.com/platform/deployment
- **Monitoring Dashboards**: https://grafana.company.com/d/platform-overview
- **Incident Response**: https://docs.company.com/platform/incidents
- **Rollback Procedures**: https://docs.company.com/platform/rollback

---

## Rollback Checklist

### Pre-Rollback
- [ ] Identify root cause of failure
- [ ] Notify stakeholders of rollback
- [ ] Prepare rollback plan
- [ ] Validate rollback procedures
- [ ] Prepare communication plan

### During Rollback
- [ ] Execute rollback steps
- [ ] Monitor system during rollback
- [ ] Communicate status updates
- [ ] Suppress false alerts
- [ ] Update incident tickets

### Post-Rollback
- [ ] Validate system stability
- [ ] Confirm traffic distribution
- [ ] Review monitoring metrics
- [ ] Document lessons learned
- [ ] Plan remediation actions

---

*Last Updated: December 16, 2025*
*Version: 1.0.0*

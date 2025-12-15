# Deployment Runbook - Event-Driven Streaming Platform

## Pre-Deployment Checklist

### 1. Code Quality Gates
- [ ] All tests passing (unit, integration, e2e)
- [ ] Code review approved
- [ ] Security scan passed
- [ ] No critical vulnerabilities
- [ ] Performance benchmarks met

### 2. Configuration Verification
- [ ] All secrets updated in secret management system
- [ ] ConfigMaps updated for new configuration
- [ ] Environment variables verified
- [ ] Database migrations reviewed and tested

### 3. Infrastructure Readiness
- [ ] Kubernetes cluster healthy
- [ ] Database backups verified
- [ ] Kafka cluster healthy
- [ ] Monitoring systems operational
- [ ] Alerting rules configured

## Deployment Procedures

### Blue-Green Deployment

1. **Deploy Green Environment**
   ```bash
   kubectl apply -f kubernetes/overlays/production/green/
   ```

2. **Verify Green Deployment**
   ```bash
   kubectl get pods -n streaming-platform -l color=green
   kubectl logs -f deployment/infrastructure-service-green -n streaming-platform
   ```

3. **Run Smoke Tests**
   ```bash
   ./scripts/smoke-tests.sh --environment=green
   ```

4. **Switch Traffic to Green**
   ```bash
   kubectl patch service streaming-platform-service \
     -n streaming-platform \
     -p '{"spec":{"selector":{"color":"green"}}}'
   ```

5. **Monitor Metrics**
   - Check error rates
   - Monitor latency (p95, p99)
   - Verify event processing rates
   - Check resource utilization

6. **Rollback if Issues**
   ```bash
   kubectl patch service streaming-platform-service \
     -n streaming-platform \
     -p '{"spec":{"selector":{"color":"blue"}}}'
   ```

### Canary Deployment

1. **Deploy Canary (10% traffic)**
   ```bash
   kubectl apply -f kubernetes/overlays/production/canary/
   ```

2. **Monitor for 30 minutes**
   - Error rates should be < 0.1%
   - Latency should be within SLA
   - No increase in failed events

3. **Gradually increase traffic**
   - 10% → 25% → 50% → 100%
   - Monitor at each stage for 15 minutes

4. **Promote to production or rollback**

## Rollback Procedures

### Quick Rollback
```bash
# Rollback to previous deployment
kubectl rollout undo deployment/infrastructure-service -n streaming-platform
kubectl rollout undo deployment/playback-service -n streaming-platform
kubectl rollout undo deployment/analytics-service -n streaming-platform
kubectl rollout undo deployment/ml-pipeline-service -n streaming-platform
```

### Database Rollback
```bash
# Rollback database migrations
flyway rollback -configFiles=flyway.conf
```

## Post-Deployment Verification

1. **Health Checks**
   ```bash
   curl https://api.streaming-platform.com/health
   curl https://api.streaming-platform.com/actuator/health
   ```

2. **Smoke Tests**
   - Create playback session
   - Pause/resume playback
   - Complete playback session
   - Verify analytics events

3. **Monitor Dashboards**
   - Grafana dashboards
   - Prometheus alerts
   - Jaeger traces

## Common Issues and Resolution

### Issue: High Error Rate
**Symptoms:** Error rate > 1%
**Resolution:**
1. Check service logs
2. Review recent changes
3. Check database connectivity
4. Verify Kafka connectivity
5. Rollback if necessary

### Issue: High Latency
**Symptoms:** p95 latency > SLA threshold
**Resolution:**
1. Check resource utilization
2. Review database query performance
3. Check Kafka consumer lag
4. Scale up if needed

### Issue: Event Processing Lag
**Symptoms:** Kafka consumer lag increasing
**Resolution:**
1. Check consumer group status
2. Scale consumer instances
3. Review event processing logic
4. Check database performance

## Emergency Contacts

- **On-Call Engineer:** [Contact Info]
- **Platform Team:** [Contact Info]
- **Security Team:** [Contact Info]
- **Database Team:** [Contact Info]

## Maintenance Windows

- **Scheduled:** Every Sunday 2-4 AM UTC
- **Emergency:** As needed with approval


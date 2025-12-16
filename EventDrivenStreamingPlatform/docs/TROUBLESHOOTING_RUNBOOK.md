# Troubleshooting Runbook

## Overview

This runbook provides systematic troubleshooting procedures for common issues in the Event-Driven Streaming Platform production environment.

## Table of Contents
1. [General Troubleshooting Process](#general-troubleshooting-process)
2. [Application Issues](#application-issues)
3. [Infrastructure Issues](#infrastructure-issues)
4. [Database Issues](#database-issues)
5. [Kafka Issues](#kafka-issues)
6. [Monitoring Issues](#monitoring-issues)
7. [Performance Issues](#performance-issues)
8. [Security Issues](#security-issues)

## General Troubleshooting Process

### Step 1: Assess Impact
```bash
# Check service status
kubectl get pods -n streaming-platform

# Check recent events
kubectl get events -n streaming-platform --sort-by=.metadata.creationTimestamp | tail -20

# Check monitoring alerts
# Go to Grafana/AlertManager
```

### Step 2: Gather Information
```bash
# Collect pod logs
kubectl logs -n streaming-platform deployment/streaming-platform-infrastructure --tail=100

# Check resource usage
kubectl top pods -n streaming-platform

# Check network connectivity
kubectl exec -n streaming-platform deployment/streaming-platform-infrastructure -- \
  curl -v http://streaming-platform-analytics:8083/actuator/health
```

### Step 3: Identify Root Cause
- Check application logs for error messages
- Review monitoring metrics for anomalies
- Check system resources (CPU, memory, disk)
- Verify network connectivity
- Check external dependencies

### Step 4: Implement Fix
- Apply appropriate fix based on diagnosis
- Monitor system during fix application
- Verify fix effectiveness

### Step 5: Document & Prevent
- Document root cause and resolution
- Update monitoring/alerting if needed
- Update runbooks with new procedures
- Implement preventive measures

## Application Issues

### Service Unavailable (HTTP 503)

**Symptoms:**
- Service returning 503 errors
- Health checks failing
- Pod restarts

**Troubleshooting Steps:**
```bash
# Check pod status
kubectl get pods -n streaming-platform -l app.kubernetes.io/component=infrastructure

# Check pod logs
kubectl logs -n streaming-platform deployment/streaming-platform-infrastructure --tail=50

# Check readiness probe
kubectl describe pod <pod-name> -n streaming-platform

# Test service endpoint
kubectl exec -n streaming-platform deployment/streaming-platform-infrastructure -- \
  curl http://localhost:8081/actuator/health
```

**Common Causes:**
- Application startup failure
- Database connection issues
- Dependency injection failures
- Health check endpoint issues

**Resolution:**
```bash
# Restart deployment
kubectl rollout restart deployment/streaming-platform-infrastructure -n streaming-platform

# Check application configuration
kubectl exec -n streaming-platform deployment/streaming-platform-infrastructure -- \
  cat /app/application.yml
```

### High Error Rate

**Symptoms:**
- Error rate > 1%
- Application logs show exceptions
- User requests failing

**Troubleshooting Steps:**
```bash
# Check application logs for errors
kubectl logs -n streaming-platform deployment/streaming-platform-infrastructure --tail=100 | grep ERROR

# Check circuit breaker status
kubectl exec -n streaming-platform deployment/streaming-platform-infrastructure -- \
  curl http://localhost:8081/actuator/circuitbreakers

# Check database connections
kubectl exec -n streaming-platform deployment/streaming-platform-infrastructure -- \
  curl http://localhost:8081/actuator/metrics | jq '.names[] | select(contains("hikaricp"))'

# Check external service status
kubectl exec -n streaming-platform deployment/streaming-platform-infrastructure -- \
  curl -f http://streaming-platform-analytics:8083/actuator/health
```

**Common Causes:**
- Database connection pool exhausted
- External service failures
- Circuit breaker open
- Application bugs

### Memory Issues

**Symptoms:**
- Pod OOM killed
- High memory usage
- Slow response times
- GC pauses

**Troubleshooting Steps:**
```bash
# Check memory usage
kubectl top pods -n streaming-platform

# Check JVM memory metrics
kubectl exec -n streaming-platform deployment/streaming-platform-infrastructure -- \
  curl http://localhost:8081/actuator/metrics | jq '.names[] | select(contains("jvm_memory"))'

# Generate heap dump (if memory is high)
kubectl exec -n streaming-platform deployment/streaming-platform-infrastructure -- \
  jcmd 1 GC.heap_dump /tmp/heap-dump.hprof

# Check for memory leaks
kubectl exec -n streaming-platform deployment/streaming-platform-infrastructure -- \
  jcmd 1 VM.flags
```

**Resolution:**
```bash
# Increase memory limits
kubectl patch deployment streaming-platform-infrastructure -n streaming-platform \
  --type json -p='[{"op": "replace", "path": "/spec/template/spec/containers/0/resources/limits/memory", "value": "2Gi"}]'

# Restart with updated JVM settings
kubectl set env deployment/streaming-platform-infrastructure -n streaming-platform \
  JAVA_OPTS="-Xmx1536m -Xms512m"
```

## Infrastructure Issues

### Pod CrashLoopBackOff

**Symptoms:**
- Pod status: CrashLoopBackOff
- Container restarting frequently
- Application not starting

**Troubleshooting Steps:**
```bash
# Check pod status and events
kubectl describe pod <pod-name> -n streaming-platform

# Check container logs
kubectl logs -n streaming-platform <pod-name> --previous

# Check resource limits
kubectl get pod <pod-name> -n streaming-platform -o yaml | grep -A 10 resources

# Check image pull status
kubectl get pod <pod-name> -n streaming-platform -o yaml | grep -A 5 image
```

**Common Causes:**
- Insufficient resources (CPU/memory)
- Image pull failures
- Configuration errors
- Health check failures

**Resolution:**
```bash
# Check and fix resource limits
kubectl patch deployment streaming-platform-infrastructure -n streaming-platform \
  --type json -p='[{"op": "replace", "path": "/spec/template/spec/containers/0/resources/limits/cpu", "value": "1000m"}]'

# Check image exists
kubectl run test-pod --image=ghcr.io/your-org/streaming-platform-infrastructure:v1.0.0 --rm -it --restart=Never -- /bin/sh
```

### Network Connectivity Issues

**Symptoms:**
- Service-to-service communication failures
- External API call failures
- DNS resolution issues

**Troubleshooting Steps:**
```bash
# Test DNS resolution
kubectl exec -n streaming-platform deployment/streaming-platform-infrastructure -- \
  nslookup streaming-platform-analytics.streaming-platform.svc.cluster.local

# Test service connectivity
kubectl exec -n streaming-platform deployment/streaming-platform-infrastructure -- \
  curl -v http://streaming-platform-analytics:8083/actuator/health

# Check network policies
kubectl get networkpolicies -n streaming-platform

# Check service endpoints
kubectl get endpoints -n streaming-platform
```

**Common Causes:**
- DNS issues
- Network policies blocking traffic
- Service discovery problems
- Firewall rules

**Resolution:**
```bash
# Check CoreDNS status
kubectl get pods -n kube-system -l k8s-app=kube-dns

# Verify network policies allow traffic
kubectl describe networkpolicy <policy-name> -n streaming-platform
```

### Storage Issues

**Symptoms:**
- Pod status: Pending
- PVC not bound
- Storage mount failures

**Troubleshooting Steps:**
```bash
# Check PVC status
kubectl get pvc -n streaming-platform

# Check storage class
kubectl get storageclass

# Check PV status
kubectl get pv

# Check pod events
kubectl describe pod <pod-name> -n streaming-platform
```

**Common Causes:**
- Insufficient storage capacity
- Storage class not available
- PVC creation failures
- Node storage issues

## Database Issues

### Connection Pool Exhausted

**Symptoms:**
- Database connection errors
- High connection count
- Slow query performance

**Troubleshooting Steps:**
```bash
# Check connection pool metrics
kubectl exec -n streaming-platform deployment/streaming-platform-infrastructure -- \
  curl http://localhost:8081/actuator/metrics | jq '.names[] | select(contains("hikaricp"))'

# Check database connections
kubectl exec -n streaming-platform -l app.kubernetes.io/name=postgresql -- \
  psql -U postgres -c "SELECT count(*) FROM pg_stat_activity;"

# Check connection limits
kubectl exec -n streaming-platform -l app.kubernetes.io/name=postgresql -- \
  psql -U postgres -c "SHOW max_connections;"
```

**Resolution:**
```bash
# Increase connection pool size
kubectl set env deployment/streaming-platform-infrastructure -n streaming-platform \
  SPRING_DATASOURCE_HIKARI_MAXIMUMPOOLSIZE=20

# Check for connection leaks
kubectl exec -n streaming-platform -l app.kubernetes.io/name=postgresql -- \
  psql -U postgres -c "SELECT * FROM pg_stat_activity WHERE state = 'idle in transaction' AND now() - query_start > interval '5 minutes';"
```

### Slow Queries

**Symptoms:**
- High query execution time
- Database CPU high
- Application timeouts

**Troubleshooting Steps:**
```bash
# Check slow query log
kubectl exec -n streaming-platform -l app.kubernetes.io/name=postgresql -- \
  psql -U postgres -c "SELECT * FROM pg_stat_statements ORDER BY total_time DESC LIMIT 10;"

# Check query plan
kubectl exec -n streaming-platform -l app.kubernetes.io/name=postgresql -- \
  psql -U postgres -c "EXPLAIN ANALYZE SELECT * FROM event_store WHERE aggregate_type = 'PlaybackSession';"

# Check index usage
kubectl exec -n streaming-platform -l app.kubernetes.io/name=postgresql -- \
  psql -U postgres -c "SELECT * FROM pg_stat_user_indexes WHERE idx_scan = 0;"

# Check table statistics
kubectl exec -n streaming-platform -l app.kubernetes.io/name=postgresql -- \
  psql -U postgres -c "ANALYZE VERBOSE;"
```

**Resolution:**
```bash
# Add missing indexes
kubectl exec -n streaming-platform -l app.kubernetes.io/name=postgresql -- \
  psql -U postgres -c "CREATE INDEX CONCURRENTLY idx_event_store_aggregate_type_created_at ON event_store (aggregate_type, created_at);"

# Update table statistics
kubectl exec -n streaming-platform -l app.kubernetes.io/name=postgresql -- \
  psql -U postgres -c "VACUUM ANALYZE event_store;"
```

## Kafka Issues

### Consumer Lag

**Symptoms:**
- High consumer lag
- Message processing delays
- Eventual consistency issues

**Troubleshooting Steps:**
```bash
# Check consumer group status
kubectl exec -n streaming-platform -l app.kubernetes.io/name=kafka -- \
  kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group analytics-service --describe

# Check consumer logs
kubectl logs -n streaming-platform deployment/streaming-platform-analytics --tail=50

# Check broker status
kubectl exec -n streaming-platform -l app.kubernetes.io/name=kafka -- \
  kafka-broker-api-versions.sh --bootstrap-server localhost:9092

# Check topic status
kubectl exec -n streaming-platform -l app.kubernetes.io/name=kafka -- \
  kafka-topics.sh --bootstrap-server localhost:9092 --list
```

**Resolution:**
```bash
# Scale consumer instances
kubectl scale deployment streaming-platform-analytics --replicas=5 -n streaming-platform

# Reset consumer offset if needed (CAUTION)
kubectl exec -n streaming-platform -l app.kubernetes.io/name=kafka -- \
  kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group analytics-service --reset-offsets --to-earliest --execute
```

### Message Loss

**Symptoms:**
- Events not processed
- Data inconsistencies
- Missing audit trail

**Troubleshooting Steps:**
```bash
# Check DLQ messages
kubectl exec -n streaming-platform -l app.kubernetes.io/name=kafka -- \
  kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic playback.events.dlq --from-beginning

# Check producer metrics
kubectl exec -n streaming-platform deployment/streaming-platform-infrastructure -- \
  curl http://localhost:8081/actuator/metrics | jq '.names[] | select(contains("kafka_producer"))'

# Check broker logs
kubectl logs -n streaming-platform -l app.kubernetes.io/name=kafka --tail=100
```

### Broker Failures

**Symptoms:**
- Kafka broker down
- Topic unavailable
- Producer/consumer failures

**Troubleshooting Steps:**
```bash
# Check broker status
kubectl get pods -n streaming-platform -l app.kubernetes.io/name=kafka

# Check broker logs
kubectl logs -n streaming-platform kafka-0 --tail=50

# Check Zookeeper status
kubectl get pods -n streaming-platform -l app.kubernetes.io/name=zookeeper
```

**Resolution:**
```bash
# Restart failed broker
kubectl delete pod kafka-0 -n streaming-platform

# Check cluster health
kubectl exec -n streaming-platform kafka-0 -- \
  kafka-cluster.sh cluster-id --bootstrap-server localhost:9092
```

## Monitoring Issues

### Missing Metrics

**Symptoms:**
- Prometheus targets down
- Metrics not collected
- Grafana dashboards empty

**Troubleshooting Steps:**
```bash
# Check Prometheus targets
kubectl port-forward -n monitoring svc/prometheus-server 9090:80
# Visit http://localhost:9090/targets

# Check service discovery
kubectl get servicemonitors -n streaming-platform

# Check Prometheus configuration
kubectl get configmap prometheus-server -n monitoring -o yaml

# Check application metrics endpoint
kubectl exec -n streaming-platform deployment/streaming-platform-infrastructure -- \
  curl http://localhost:8081/actuator/prometheus
```

### Alert Not Firing

**Symptoms:**
- Expected alerts not triggering
- Incident not detected

**Troubleshooting Steps:**
```bash
# Check alert rules
kubectl get prometheusrules -n monitoring

# Test alert condition manually
# Go to Prometheus and test the query

# Check AlertManager configuration
kubectl get configmap alertmanager -n monitoring -o yaml

# Check alert history
kubectl port-forward -n monitoring svc/alertmanager 9093:9093
# Visit http://localhost:9093
```

## Performance Issues

### High Latency

**Symptoms:**
- Response time > 100ms (95th percentile)
- User experience degraded
- SLA breaches

**Troubleshooting Steps:**
```bash
# Check application metrics
kubectl exec -n streaming-platform deployment/streaming-platform-infrastructure -- \
  curl http://localhost:8081/actuator/metrics | jq '.names[] | select(contains("http_server_requests_seconds"))'

# Check JVM performance
kubectl exec -n streaming-platform deployment/streaming-platform-infrastructure -- \
  jcmd 1 VM.flags

# Check database performance
kubectl exec -n streaming-platform -l app.kubernetes.io/name=postgresql -- \
  psql -U postgres -c "SELECT * FROM pg_stat_activity WHERE state = 'active';"

# Check network latency
kubectl exec -n streaming-platform deployment/streaming-platform-infrastructure -- \
  ping streaming-platform-postgresql
```

**Resolution:**
```bash
# Scale horizontally
kubectl scale deployment streaming-platform-infrastructure --replicas=5 -n streaming-platform

# Optimize JVM settings
kubectl set env deployment/streaming-platform-infrastructure -n streaming-platform \
  JAVA_OPTS="-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:G1HeapRegionSize=16m"

# Add database indexes
kubectl exec -n streaming-platform -l app.kubernetes.io/name=postgresql -- \
  psql -U postgres -c "CREATE INDEX CONCURRENTLY idx_event_store_correlation_id_timestamp ON event_store (correlation_id, created_at);"
```

### High Resource Usage

**Symptoms:**
- CPU > 80%
- Memory > 85%
- Resource exhaustion

**Troubleshooting Steps:**
```bash
# Check resource usage
kubectl top pods -n streaming-platform

# Check application threads
kubectl exec -n streaming-platform deployment/streaming-platform-infrastructure -- \
  jcmd 1 Thread.print | head -50

# Check for memory leaks
kubectl exec -n streaming-platform deployment/streaming-platform-infrastructure -- \
  jcmd 1 GC.heap_info

# Profile application
kubectl exec -n streaming-platform deployment/streaming-platform-infrastructure -- \
  jcmd 1 JFR.start duration=60s filename=/tmp/profile.jfr
```

## Security Issues

### Authentication Failures

**Symptoms:**
- Login failures
- JWT token validation errors
- Unauthorized access attempts

**Troubleshooting Steps:**
```bash
# Check JWT configuration
kubectl get secret jwt-secret -n streaming-platform -o yaml

# Check authentication logs
kubectl logs -n streaming-platform deployment/streaming-platform-infrastructure --tail=100 | grep -i auth

# Verify certificate validity
kubectl get certificate -n streaming-platform

# Check OAuth2 configuration
kubectl exec -n streaming-platform deployment/streaming-platform-infrastructure -- \
  curl http://localhost:8081/actuator/env | jq '.propertySources[] | select(.name | contains("applicationConfig")) | .properties'
```

### Authorization Issues

**Symptoms:**
- Access denied errors
- RBAC policy failures
- Permission validation errors

**Troubleshooting Steps:**
```bash
# Check RBAC configuration
kubectl get roles,rolebindings -n streaming-platform

# Check service account permissions
kubectl get serviceaccounts -n streaming-platform

# Verify user roles
kubectl exec -n streaming-platform deployment/streaming-platform-infrastructure -- \
  curl http://localhost:8081/actuator/env | grep -i role
```

### SSL/TLS Issues

**Symptoms:**
- Certificate validation failures
- HTTPS connection errors
- Mixed content warnings

**Troubleshooting Steps:**
```bash
# Check certificate status
kubectl get certificate -n streaming-platform

# Verify certificate validity
kubectl describe certificate streaming-platform-tls -n streaming-platform

# Check ingress TLS configuration
kubectl describe ingress streaming-platform-ingress -n streaming-platform

# Test SSL connection
openssl s_client -connect api.streaming-platform.company.com:443 -servername api.streaming-platform.company.com
```

---

## Automated Troubleshooting Scripts

### Health Check Script
```bash
#!/bin/bash
# comprehensive-health-check.sh

NAMESPACE="streaming-platform"
SERVICES=("infrastructure" "analytics" "playback" "ml-pipeline")

echo "=== Comprehensive Health Check ==="

# Check pod status
echo "Pod Status:"
kubectl get pods -n $NAMESPACE

# Check service endpoints
echo -e "\nService Health:"
for service in "${SERVICES[@]}"; do
  health=$(kubectl exec -n $NAMESPACE deployment/streaming-platform-$service -- \
    curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/actuator/health)
  echo "$service: HTTP $health"
done

# Check resource usage
echo -e "\nResource Usage:"
kubectl top pods -n $NAMESPACE

# Check recent errors
echo -e "\nRecent Errors:"
kubectl logs -n $NAMESPACE --tail=10 --all-containers=true | grep -i error | tail -5

echo "=== Health Check Complete ==="
```

### Performance Diagnostic Script
```bash
#!/bin/bash
# performance-diagnostic.sh

NAMESPACE="streaming-platform"
SERVICE="infrastructure"

echo "=== Performance Diagnostic ==="

# JVM diagnostics
echo "JVM Memory:"
kubectl exec -n $NAMESPACE deployment/streaming-platform-$SERVICE -- \
  jcmd 1 VM.info | grep -E "(heap|gc)"

# Database connections
echo "Database Connections:"
kubectl exec -n $NAMESPACE deployment/streaming-platform-$SERVICE -- \
  curl -s http://localhost:8081/actuator/metrics | jq '.names[] | select(contains("hikaricp"))'

# HTTP metrics
echo "HTTP Performance:"
kubectl exec -n $NAMESPACE deployment/streaming-platform-$SERVICE -- \
  curl -s http://localhost:8081/actuator/metrics | jq '.names[] | select(contains("http_server_requests_seconds"))'

echo "=== Diagnostic Complete ==="
```

---

## Escalation Procedures

### Severity Levels

**SEV 1 (Critical)**
- Complete system outage
- Data loss or corruption
- Security breach
- Escalation: Immediate, all hands

**SEV 2 (High)**
- Major functionality degraded
- Performance severely impacted
- User-facing errors > 50%
- Escalation: Within 30 minutes

**SEV 3 (Medium)**
- Minor functionality issues
- Performance moderately impacted
- User-facing errors 10-50%
- Escalation: Within 2 hours

**SEV 4 (Low)**
- Non-critical issues
- Monitoring alerts
- Internal tool issues
- Escalation: Within 24 hours

### Escalation Contacts

- **Primary On-Call**: Platform Team Lead
- **Secondary On-Call**: DevOps Engineer
- **Management**: Engineering Manager
- **External**: Infrastructure Provider Support

---

## Prevention Measures

### Regular Maintenance
- [ ] Update dependencies monthly
- [ ] Review and rotate secrets quarterly
- [ ] Update base images weekly
- [ ] Review monitoring alerts monthly

### Proactive Monitoring
- [ ] Set up synthetic monitoring
- [ ] Implement chaos engineering
- [ ] Regular load testing
- [ ] Dependency health checks

### Documentation Updates
- [ ] Update runbooks after incidents
- [ ] Document new troubleshooting procedures
- [ ] Update monitoring dashboards
- [ ] Review alert thresholds

---

*Last Updated: December 16, 2025*
*Version: 1.0.0*

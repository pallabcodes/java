# Load Testing & Validation Scripts

This directory contains comprehensive load testing and validation scripts for the Event-Driven Streaming Platform.

## Overview

The load testing suite includes:
- **Load Tests**: Simulate production traffic patterns
- **Smoke Tests**: Basic functionality validation
- **Chaos Tests**: Resilience testing during failures
- **Post-Deployment Validation**: Comprehensive system validation

## Prerequisites

### K6 Installation
```bash
# Install K6 for load testing
curl -sSL https://github.com/grafana/k6/releases/download/v0.45.0/k6-v0.45.0-linux-amd64.tar.gz | tar xvz
sudo mv k6-v0.45.0-linux-amd64/k6 /usr/local/bin/
```

### Environment Variables
```bash
export BASE_URL=https://api.streaming-platform.company.com
export K6_PROMETHEUS_RW_SERVER_URL=http://prometheus-pushgateway:9091
```

## Test Scripts

### 1. Full Load Test (`streaming-platform-load-test.js`)
Comprehensive load testing simulating real user traffic patterns.

**Test Scenarios:**
- Health checks (10% of requests)
- Start playback sessions (30% of requests)
- Get playback status (25% of requests)
- Update playback position (20% of requests)
- Stop playback (10% of requests)
- Analytics queries (5% of requests)

**Load Profile:**
- Ramp up: 0 → 100 users (2 minutes)
- Sustained: 100 users (5 minutes)
- Ramp up: 100 → 500 users (3 minutes)
- Main test: 500 users (10 minutes)
- Stress test: 500 → 1000 users (5 minutes)
- Peak test: 1000 users (5 minutes)
- Ramp down: 1000 → 0 users (2 minutes)

**Run the test:**
```bash
# Basic run
k6 run streaming-platform-load-test.js

# With custom base URL
k6 run -e BASE_URL=https://staging-api.streaming-platform.company.com streaming-platform-load-test.js

# With Prometheus output
k6 run --out prometheus streaming-platform-load-test.js
```

### 2. Smoke Test (`smoke-test.js`)
Basic functionality validation for quick checks.

**Validates:**
- Service health endpoints
- Basic API connectivity
- Response times under minimal load

**Run the test:**
```bash
k6 run smoke-test.js
```

### 3. Chaos Test (`chaos-test.js`)
Resilience testing during simulated failures.

**Use with Chaos Mesh:**
```bash
# Install Chaos Mesh
curl -sSL https://mirrors.chaos-mesh.org/v2.5.1/install.sh | bash

# Apply chaos experiments
kubectl apply -f chaos-engineering/experiments/

# Run chaos test
k6 run chaos-test.js

# Clean up chaos experiments
kubectl delete -f chaos-engineering/experiments/
```

## Post-Deployment Validation

### Comprehensive Validation Script (`post-deployment-validation.sh`)

Runs 30+ validation checks across all system components.

**Validates:**
- Infrastructure (Kubernetes, pods, services)
- Containers (health, metrics, logs)
- Databases (PostgreSQL connectivity, schemas)
- Message queues (Kafka topics, brokers)
- Cache (Redis connectivity)
- Networking (ingress, SSL, DNS)
- Monitoring (Prometheus, Grafana, alerts)
- APIs (endpoints, authentication, security)
- Performance (response times, error rates)
- Business logic (playback, analytics)
- Chaos engineering (circuit breakers, resilience)

**Run validation:**
```bash
# Basic validation
./post-deployment-validation.sh

# With custom parameters
NAMESPACE=my-namespace RELEASE_NAME=my-release BASE_URL=https://my-api.com ./post-deployment-validation.sh
```

## Performance Thresholds

### Load Test Thresholds
- **Response Time (95th percentile)**: < 100ms
- **Error Rate**: < 1%
- **Throughput**: > 10,000 requests/minute
- **Memory Usage**: < 85%
- **CPU Usage**: < 80%

### Smoke Test Thresholds
- **Response Time (95th percentile)**: < 500ms
- **Error Rate**: < 10%
- **Availability**: 100%

### Chaos Test Thresholds
- **Response Time (95th percentile)**: < 200ms (higher tolerance during chaos)
- **Error Rate**: < 5% (some failures expected during chaos)
- **Recovery Time**: < 30 seconds

## Monitoring Integration

### Prometheus Metrics
Load test results are automatically exported to Prometheus:

```bash
# Run test with Prometheus output
k6 run --out prometheus streaming-platform-load-test.js

# View metrics in Prometheus
# http://prometheus:9090/graph
```

### Grafana Dashboards
Load test dashboards show:
- Response time distributions
- Error rates over time
- Throughput graphs
- Resource utilization
- Test scenario breakdowns

## CI/CD Integration

### GitHub Actions Integration
```yaml
# In .github/workflows/production-deployment.yml
- name: Load Testing
  run: |
    k6 run --out prometheus load-testing/streaming-platform-load-test.js

- name: Post-Deployment Validation
  run: |
    chmod +x load-testing/post-deployment-validation.sh
    ./load-testing/post-deployment-validation.sh
```

### Automated Threshold Checks
```bash
# Check if load test passed thresholds
k6 run --threshold 'http_req_duration{expected_response:"true"}:p(95)<100' streaming-platform-load-test.js
```

## Troubleshooting

### Common Issues

#### K6 Installation Issues
```bash
# Check K6 version
k6 version

# Install specific version
curl -sSL https://github.com/grafana/k6/releases/download/v0.45.0/k6-v0.45.0-linux-amd64.tar.gz | tar xvz
sudo cp k6-v0.45.0-linux-amd64/k6 /usr/local/bin/
```

#### Test Timeout Issues
```bash
# Increase timeout
k6 run --http-timeout 30s streaming-platform-load-test.js

# Check target system responsiveness
curl -w "@curl-format.txt" -o /dev/null -s $BASE_URL/v1/health
```

#### Resource Constraints
```bash
# Run with lower concurrency
k6 run --vus 10 --duration 1m streaming-platform-load-test.js

# Check system resources
kubectl top pods -n streaming-platform
kubectl top nodes
```

#### Authentication Issues
```bash
# Set proper auth token
export AUTH_TOKEN="your-jwt-token"
k6 run -e AUTH_TOKEN=$AUTH_TOKEN streaming-platform-load-test.js
```

## Best Practices

### Test Environment Setup
1. **Dedicated Test Environment**: Use staging/production-like environment
2. **Data Isolation**: Use test data that doesn't affect production
3. **Resource Allocation**: Ensure sufficient resources for load testing
4. **Monitoring**: Monitor both application and infrastructure metrics

### Test Execution
1. **Gradual Load Increase**: Start with low load, gradually increase
2. **Realistic Scenarios**: Use traffic patterns matching production
3. **Duration**: Run tests long enough to detect memory leaks, etc.
4. **Multiple Runs**: Run tests multiple times for consistency

### Result Analysis
1. **Threshold Compliance**: Ensure all performance thresholds met
2. **Error Analysis**: Investigate all errors, not just rates
3. **Resource Monitoring**: Check for resource bottlenecks
4. **Scalability Validation**: Confirm horizontal scaling works

## Integration with Chaos Engineering

### Combined Testing Approach
```bash
# Start chaos experiments in background
kubectl apply -f chaos-engineering/pod-kill.yaml &

# Run load test during chaos
k6 run chaos-test.js

# Check system resilience
kubectl get pods -n streaming-platform
```

### Chaos Experiments
- **Pod Kills**: Test Kubernetes self-healing
- **Network Latency**: Test timeout handling
- **Resource Exhaustion**: Test circuit breakers
- **Database Failures**: Test connection pooling

## Reporting

### Load Test Reports
- **Performance Metrics**: Response times, throughput, error rates
- **Resource Utilization**: CPU, memory, network usage
- **Bottleneck Identification**: Slowest components, failure points
- **Recommendations**: Scaling suggestions, optimization opportunities

### Validation Reports
- **Check Results**: Pass/fail status for all validation checks
- **Failure Analysis**: Root cause for any failed checks
- **Remediation Steps**: How to fix identified issues
- **Go/No-Go Decision**: Whether deployment can proceed

---

## Quick Start

```bash
# 1. Install K6
curl -sSL https://github.com/grafana/k6/releases/download/v0.45.0/k6-v0.45.0-linux-amd64.tar.gz | tar xvz
sudo mv k6-v0.45.0-linux-amd64/k6 /usr/local/bin/

# 2. Set environment
export BASE_URL=https://your-api-endpoint.com

# 3. Run smoke test
k6 run load-testing/smoke-test.js

# 4. Run full load test
k6 run load-testing/streaming-platform-load-test.js

# 5. Run post-deployment validation
./load-testing/post-deployment-validation.sh
```

This comprehensive testing suite ensures your Event-Driven Streaming Platform is production-ready and can handle real-world traffic patterns.

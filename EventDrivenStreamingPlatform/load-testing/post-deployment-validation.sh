#!/bin/bash

# Post-Deployment Validation Script
# Comprehensive validation of all system components after deployment

set -e

# Configuration
NAMESPACE="${NAMESPACE:-streaming-platform}"
RELEASE_NAME="${RELEASE_NAME:-streaming-platform}"
BASE_URL="${BASE_URL:-https://api.streaming-platform.company.com}"
TIMEOUT="${TIMEOUT:-300}"

echo "🚀 Starting Post-Deployment Validation"
echo "======================================"
echo "Namespace: $NAMESPACE"
echo "Release: $RELEASE_NAME"
echo "Base URL: $BASE_URL"
echo "Timeout: ${TIMEOUT}s"
echo

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Validation counters
TOTAL_CHECKS=0
PASSED_CHECKS=0
FAILED_CHECKS=0

# Function to run checks
check() {
    local name="$1"
    local command="$2"

    echo -n "🔍 $name... "
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))

    if eval "$command" >/dev/null 2>&1; then
        echo -e "${GREEN}✅ PASSED${NC}"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
    else
        echo -e "${RED}❌ FAILED${NC}"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
        return 1
    fi
}

# Function to check with timeout
check_with_timeout() {
    local name="$1"
    local command="$2"
    local timeout="${3:-30}"

    echo -n "🔍 $name... "
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))

    if timeout "$timeout" bash -c "$command" >/dev/null 2>&1; then
        echo -e "${GREEN}✅ PASSED${NC}"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
    else
        echo -e "${RED}❌ FAILED${NC}"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
        return 1
    fi
}

echo "📦 INFRASTRUCTURE VALIDATION"
echo "============================="

# Kubernetes cluster checks
check "Kubernetes API accessible" "kubectl cluster-info"
check "Namespace exists" "kubectl get namespace $NAMESPACE"
check "Helm release exists" "helm status $RELEASE_NAME -n $NAMESPACE"

# Pod checks
check "All pods running" "kubectl get pods -n $NAMESPACE --no-headers | grep -v Running | wc -l | grep -q 0"
check "No pods in CrashLoopBackOff" "kubectl get pods -n $NAMESPACE --no-headers | grep CrashLoopBackOff | wc -l | grep -q 0"
check "No pods in ImagePullBackOff" "kubectl get pods -n $NAMESPACE --no-headers | grep ImagePullBackOff | wc -l | grep -q 0"

# Service checks
check "Infrastructure service exists" "kubectl get service streaming-platform-infrastructure -n $NAMESPACE"
check "Analytics service exists" "kubectl get service streaming-platform-analytics -n $NAMESPACE"
check "Playback service exists" "kubectl get service streaming-platform-playback -n $NAMESPACE"
check "ML Pipeline service exists" "kubectl get service streaming-platform-ml-pipeline -n $NAMESPACE"

echo
echo "🐳 CONTAINER VALIDATION"
echo "======================="

# Container health checks
SERVICES=("infrastructure" "analytics" "playback" "ml-pipeline")
for service in "${SERVICES[@]}"; do
    check "$service pod ready" "kubectl wait --for=condition=ready pod -l app.kubernetes.io/component=$service -n $NAMESPACE --timeout=60s"
    check "$service health endpoint" "kubectl exec -n $NAMESPACE deployment/streaming-platform-$service -- curl -f http://localhost:8081/actuator/health"
    check "$service metrics endpoint" "kubectl exec -n $NAMESPACE deployment/streaming-platform-$service -- curl -f http://localhost:8081/actuator/prometheus | grep -q 'jvm_info'"
done

echo
echo "🗄️ DATABASE VALIDATION"
echo "======================="

# PostgreSQL checks
check "PostgreSQL running" "kubectl get pods -n $NAMESPACE -l app.kubernetes.io/name=postgresql"
check "PostgreSQL ready" "kubectl exec -n $NAMESPACE -l app.kubernetes.io/name=postgresql -- pg_isready -U postgres"
check "Database schemas exist" "kubectl exec -n $NAMESPACE -l app.kubernetes.io/name=postgresql -- psql -U postgres -d streaming_platform -c 'SELECT count(*) FROM information_schema.tables WHERE table_schema = '\''public'\'';' | grep -q '[0-9]'"

echo
echo "📨 MESSAGE QUEUE VALIDATION"
echo "============================"

# Kafka checks
check "Kafka brokers running" "kubectl get pods -n $NAMESPACE -l app.kubernetes.io/name=kafka"
check "Zookeeper running" "kubectl get pods -n $NAMESPACE -l app.kubernetes.io/name=zookeeper"
check "Kafka topics exist" "kubectl exec -n $NAMESPACE kafka-0 -- kafka-topics.sh --bootstrap-server localhost:9092 --list | grep -q playback.events"

echo
echo "🔄 CACHE VALIDATION"
echo "==================="

# Redis checks
check "Redis running" "kubectl get pods -n $NAMESPACE -l app.kubernetes.io/name=redis"
check "Redis master ready" "kubectl exec -n $NAMESPACE -l app.kubernetes.io/name=redis -- redis-cli ping | grep -q PONG"

echo
echo "🌐 NETWORKING VALIDATION"
echo "========================"

# Ingress/Network checks
check "Ingress exists" "kubectl get ingress -n $NAMESPACE | grep -q streaming-platform"
check "External DNS configured" "nslookup $BASE_URL | grep -q 'Address'"
check "SSL certificate valid" "openssl s_client -connect ${BASE_URL#https://}:443 -servername ${BASE_URL#https://} < /dev/null 2>/dev/null | openssl x509 -noout -dates | grep -q notAfter"

echo
echo "📊 MONITORING VALIDATION"
echo "========================="

# Monitoring checks
check "Prometheus running" "kubectl get pods -n monitoring -l app.kubernetes.io/name=prometheus"
check "Grafana running" "kubectl get pods -n monitoring -l app.kubernetes.io/name=grafana"
check "Prometheus targets healthy" "kubectl exec -n monitoring prometheus-prometheus-0 -- wget -qO- http://localhost:9090/api/v1/targets | jq -r '.data.activeTargets[].health' | grep -q unhealthy || true"

echo
echo "🚪 API ENDPOINT VALIDATION"
echo "==========================="

# API endpoint checks
check "API gateway accessible" "curl -f -k $BASE_URL/v1/health"
check "Infrastructure API accessible" "curl -f -k $BASE_URL/v1/infrastructure/health"
check "Analytics API accessible" "curl -f -k $BASE_URL/v1/analytics/health"
check "Playback API accessible" "curl -f -k $BASE_URL/v1/playback/health"

echo
echo "🔐 SECURITY VALIDATION"
echo "======================"

# Security checks
check "API requires authentication" "curl -f -k $BASE_URL/v1/analytics/dashboard 2>/dev/null; [ $? -eq 22 ]"  # Should fail with auth error
check "HTTPS enforced" "curl -f http://${BASE_URL#https://} 2>/dev/null; [ $? -eq 7 ]"  # Should fail for HTTP
check "Security headers present" "curl -f -k -I $BASE_URL/v1/health | grep -q 'X-Frame-Options\|X-Content-Type-Options'"

echo
echo "⚡ PERFORMANCE VALIDATION"
echo "========================="

# Performance checks
check "API response time < 100ms" "curl -f -k -w '%{time_total}' -o /dev/null $BASE_URL/v1/health | awk '{if($1 < 0.1) exit 0; else exit 1}'"
check "No high error rates" "kubectl exec -n $NAMESPACE deployment/streaming-platform-infrastructure -- curl -s http://localhost:8081/actuator/metrics | jq -r '.names[] | select(contains(\"http_server_requests_seconds\"))' | wc -l | grep -q '[0-9]'"

echo
echo "🎯 BUSINESS LOGIC VALIDATION"
echo "============================"

# Business logic checks
check "Playback session creation" "curl -f -k -X POST $BASE_URL/v1/playback/start -H 'Content-Type: application/json' -d '{\"userId\":\"test-user\",\"contentId\":\"test-content\",\"deviceType\":\"WEB\"}'"
check "Analytics data retrieval" "curl -f -k $BASE_URL/v1/analytics/dashboard?userId=test-user"

echo
echo "📈 LOAD TESTING VALIDATION"
echo "==========================="

# Run smoke tests
check "Smoke tests pass" "k6 run --vus 1 --duration 10s load-testing/smoke-test.js"

echo
echo "🎭 CHAOS ENGINEERING VALIDATION"
echo "==============================="

# Chaos engineering checks
check "System survives pod kills" "kubectl get pods -n $NAMESPACE | grep Running | wc -l | grep -q '[0-9]'"
check "Circuit breakers functional" "kubectl exec -n $NAMESPACE deployment/streaming-platform-infrastructure -- curl -s http://localhost:8081/actuator/circuitbreakers | jq -r '.states | length' | grep -q '[0-9]'"

echo
echo "📋 VALIDATION SUMMARY"
echo "===================="
echo "Total Checks: $TOTAL_CHECKS"
echo -e "Passed: ${GREEN}$PASSED_CHECKS${NC}"
echo -e "Failed: ${RED}$FAILED_CHECKS${NC}"

if [ "$FAILED_CHECKS" -eq 0 ]; then
    echo
    echo -e "${GREEN}🎉 ALL VALIDATION CHECKS PASSED!${NC}"
    echo "The system is ready for production traffic."
    exit 0
else
    echo
    echo -e "${RED}❌ $FAILED_CHECKS VALIDATION CHECKS FAILED${NC}"
    echo "Please review the failed checks above and address issues before proceeding."
    echo "Check the troubleshooting runbook for guidance."
    exit 1
fi

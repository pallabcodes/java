#!/bin/bash

# Payments Platform Security Audit Script
# This script performs comprehensive security and compliance checks

set -euo pipefail

# Configuration
CLUSTER_NAME="payments-platform-prod"
NAMESPACE="payments-platform"
REPORT_DIR="audit-reports"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
REPORT_FILE="$REPORT_DIR/security_audit_$TIMESTAMP.json"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Initialize audit results
audit_results="{}"

# Function to update audit results
update_audit_result() {
    local check_name="$1"
    local status="$2"
    local details="$3"

    audit_results=$(echo "$audit_results" | jq --arg check "$check_name" --arg status "$status" --arg details "$details" \
        '. + {($check): {"status": $status, "details": $details, "timestamp": "'$(date -Iseconds)'"}}')
}

# Function to run kubectl command with error handling
kubectl_safe() {
    local cmd="$1"
    local description="$2"

    if result=$(eval "$cmd" 2>/dev/null); then
        echo "$result"
    else
        log_warning "Failed to execute: $description"
        echo ""
    fi
}

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Pre-flight checks
preflight_checks() {
    log_info "Running pre-flight checks..."

    local missing_tools=()

    if ! command_exists kubectl; then
        missing_tools+=("kubectl")
    fi

    if ! command_exists jq; then
        missing_tools+=("jq")
    fi

    if ! command_exists trivy; then
        missing_tools+=("trivy")
    fi

    if [ ${#missing_tools[@]} -ne 0 ]; then
        log_error "Missing required tools: ${missing_tools[*]}"
        log_info "Please install missing tools and try again"
        exit 1
    fi

    # Check kubectl context
    if ! kubectl cluster-info >/dev/null 2>&1; then
        log_error "kubectl is not configured or cluster is not accessible"
        exit 1
    fi

    log_success "Pre-flight checks passed"
}

# Security Context Audit
audit_security_contexts() {
    log_info "Auditing security contexts..."

    # Check pods without security contexts
    local insecure_pods
    insecure_pods=$(kubectl get pods -n "$NAMESPACE" -o jsonpath='{range .items[*]}{.metadata.name}{"\t"}{.spec.securityContext.runAsNonRoot}{"\t"}{.spec.containers[0].securityContext.runAsNonRoot}{"\n"}{end}' | grep -E "(<no value>|\s*$)" || true)

    if [ -n "$insecure_pods" ]; then
        update_audit_result "security_contexts" "FAIL" "Pods found without proper security contexts: $insecure_pods"
        log_error "Security context violations found"
    else
        update_audit_result "security_contexts" "PASS" "All pods have proper security contexts"
        log_success "Security contexts are properly configured"
    fi
}

# Network Policy Audit
audit_network_policies() {
    log_info "Auditing network policies..."

    local policy_count
    policy_count=$(kubectl get networkpolicies -n "$NAMESPACE" --no-headers | wc -l)

    if [ "$policy_count" -eq 0 ]; then
        update_audit_result "network_policies" "FAIL" "No network policies found in namespace"
        log_error "Network policies are missing"
    else
        update_audit_result "network_policies" "PASS" "Found $policy_count network policies"
        log_success "Network policies are configured"
    fi
}

# Image Security Audit
audit_container_images() {
    log_info "Auditing container images..."

    local images
    images=$(kubectl get pods -n "$NAMESPACE" -o jsonpath='{range .items[*]}{.spec.containers[*].image}{"\n"}{end}' | sort | uniq)

    local vulnerable_images=()

    while IFS= read -r image; do
        if [ -n "$image" ]; then
            log_info "Scanning image: $image"
            if ! trivy image --exit-code 1 --no-progress "$image" >/dev/null 2>&1; then
                vulnerable_images+=("$image")
                log_warning "Vulnerable image found: $image"
            fi
        fi
    done <<< "$images"

    if [ ${#vulnerable_images[@]} -ne 0 ]; then
        update_audit_result "container_images" "FAIL" "Vulnerable images found: ${vulnerable_images[*]}"
        log_error "Container image security issues detected"
    else
        update_audit_result "container_images" "PASS" "All container images are secure"
        log_success "Container images passed security scan"
    fi
}

# RBAC Audit
audit_rbac() {
    log_info "Auditing RBAC configuration..."

    # Check for overly permissive roles
    local permissive_roles
    permissive_roles=$(kubectl get clusterroles,roles -n "$NAMESPACE" -o json | jq -r '.items[] | select(.rules[]? | select(.verbs[]? == "*" or .resources[]? == "*")) | .metadata.name' 2>/dev/null || true)

    if [ -n "$permissive_roles" ]; then
        update_audit_result "rbac_permissions" "WARNING" "Overly permissive roles found: $permissive_roles"
        log_warning "RBAC permissions are too permissive"
    else
        update_audit_result "rbac_permissions" "PASS" "RBAC permissions are properly restricted"
        log_success "RBAC configuration is secure"
    fi
}

# Secret Management Audit
audit_secrets() {
    log_info "Auditing secret management..."

    # Check for secrets in environment variables
    local env_secrets
    env_secrets=$(kubectl get pods -n "$NAMESPACE" -o json | jq -r '.items[] | select(.spec.containers[]?.env[]?.valueFrom?.secretKeyRef != null) | .metadata.name' 2>/dev/null || true)

    if [ -n "$env_secrets" ]; then
        update_audit_result "secret_management" "WARNING" "Secrets found in environment variables: $env_secrets"
        log_warning "Secrets are exposed in environment variables"
    else
        update_audit_result "secret_management" "PASS" "Secrets are properly managed"
        log_success "Secret management is secure"
    fi
}

# Compliance Checks
audit_compliance() {
    log_info "Running compliance checks..."

    # PCI DSS compliance checks
    local pci_labels
    pci_labels=$(kubectl get pods -n "$NAMESPACE" -o json | jq -r '.items[] | select(.metadata.labels["compliance/pci-dss"] != "required") | .metadata.name' 2>/dev/null || true)

    if [ -n "$pci_labels" ]; then
        update_audit_result "pci_dss_compliance" "FAIL" "Pods missing PCI DSS compliance labels: $pci_labels"
        log_error "PCI DSS compliance violations found"
    else
        update_audit_result "pci_dss_compliance" "PASS" "All pods are PCI DSS compliant"
        log_success "PCI DSS compliance verified"
    fi

    # GDPR compliance checks
    local gdpr_labels
    gdpr_labels=$(kubectl get pods -n "$NAMESPACE" -o json | jq -r '.items[] | select(.metadata.labels["compliance/gdpr"] != "required") | .metadata.name' 2>/dev/null || true)

    if [ -n "$gdpr_labels" ]; then
        update_audit_result "gdpr_compliance" "FAIL" "Pods missing GDPR compliance labels: $gdpr_labels"
        log_error "GDPR compliance violations found"
    else
        update_audit_result "gdpr_compliance" "PASS" "All pods are GDPR compliant"
        log_success "GDPR compliance verified"
    fi
}

# Performance and Resource Audit
audit_resources() {
    log_info "Auditing resource usage..."

    # Check for pods without resource limits
    local unlimited_resources
    unlimited_resources=$(kubectl get pods -n "$NAMESPACE" -o json | jq -r '.items[] | select(.spec.containers[]?.resources.limits == null) | .metadata.name' 2>/dev/null || true)

    if [ -n "$unlimited_resources" ]; then
        update_audit_result "resource_limits" "FAIL" "Pods without resource limits: $unlimited_resources"
        log_error "Resource limits are missing"
    else
        update_audit_result "resource_limits" "PASS" "All pods have resource limits"
        log_success "Resource limits are properly configured"
    fi
}

# Generate audit report
generate_report() {
    log_info "Generating audit report..."

    mkdir -p "$REPORT_DIR"

    # Add metadata to the report
    audit_results=$(echo "$audit_results" | jq --arg cluster "$CLUSTER_NAME" --arg namespace "$NAMESPACE" \
        '. + {"metadata": {"cluster": $cluster, "namespace": $namespace, "audit_date": "'$(date -Iseconds)'", "auditor": "security-audit-script"}}')

    echo "$audit_results" | jq '.' > "$REPORT_FILE"

    # Calculate summary
    local total_checks
    local passed_checks
    local failed_checks
    local warning_checks

    total_checks=$(echo "$audit_results" | jq 'keys | length')
    passed_checks=$(echo "$audit_results" | jq '[.[] | select(.status == "PASS")] | length')
    failed_checks=$(echo "$audit_results" | jq '[.[] | select(.status == "FAIL")] | length')
    warning_checks=$(echo "$audit_results" | jq '[.[] | select(.status == "WARNING")] | length')

    log_info "Audit Summary:"
    log_success "Total checks: $total_checks"
    log_success "Passed: $passed_checks"
    log_warning "Warnings: $warning_checks"
    log_error "Failed: $failed_checks"

    if [ "$failed_checks" -gt 0 ]; then
        log_error "Audit completed with failures. Review the report: $REPORT_FILE"
        return 1
    elif [ "$warning_checks" -gt 0 ]; then
        log_warning "Audit completed with warnings. Review the report: $REPORT_FILE"
        return 0
    else
        log_success "Audit completed successfully. Report saved to: $REPORT_FILE"
        return 0
    fi
}

# Main execution
main() {
    log_info "Starting Payments Platform Security Audit"
    log_info "Cluster: $CLUSTER_NAME"
    log_info "Namespace: $NAMESPACE"
    log_info "Timestamp: $(date)"

    # Run pre-flight checks
    preflight_checks

    # Run all audit checks
    audit_security_contexts
    audit_network_policies
    audit_container_images
    audit_rbac
    audit_secrets
    audit_compliance
    audit_resources

    # Generate and display results
    if generate_report; then
        log_success "Security audit completed successfully"
        exit 0
    else
        log_error "Security audit completed with issues"
        exit 1
    fi
}

# Run main function
main "$@"

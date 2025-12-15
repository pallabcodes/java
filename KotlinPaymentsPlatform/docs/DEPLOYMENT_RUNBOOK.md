# Deployment Runbook - Kotlin Payments Platform

## Pre-Deployment Checklist

### 1. Code Quality Gates
- [ ] All tests passing (unit, integration)
- [ ] Code review approved
- [ ] Security scan passed
- [ ] No critical vulnerabilities
- [ ] PCI DSS compliance verified

### 2. Configuration Verification
- [ ] Payment gateway credentials updated
- [ ] Database credentials rotated if needed
- [ ] Redis configuration verified
- [ ] Risk rules reviewed
- [ ] Compliance settings verified

### 3. Infrastructure Readiness
- [ ] Kubernetes cluster healthy
- [ ] Database backups verified
- [ ] Redis cluster healthy
- [ ] Monitoring systems operational
- [ ] Alerting rules configured

## Deployment Procedures

### Rolling Update Deployment

1. **Update Deployment**
   ```bash
   kubectl set image deployment/payments-service \
     payments-service=registry/payments-platform/payments:$VERSION \
     -n payments-platform
   ```

2. **Monitor Rollout**
   ```bash
   kubectl rollout status deployment/payments-service -n payments-platform
   ```

3. **Verify Health**
   ```bash
   kubectl get pods -n payments-platform
   curl https://api.payments-platform.com/api/v1/payments/health
   ```

4. **Run Smoke Tests**
   ```bash
   ./scripts/smoke-tests.sh
   ```

### Canary Deployment

1. **Deploy Canary (5% traffic)**
   ```bash
   kubectl apply -f helm/payments-platform/values-canary.yaml
   ```

2. **Monitor for 1 hour**
   - Payment success rate should be > 99.5%
   - Risk evaluation latency < 200ms p95
   - No increase in fraud detection false positives

3. **Gradually increase traffic**
   - 5% → 15% → 30% → 50% → 100%
   - Monitor at each stage for 30 minutes

4. **Promote or rollback**

## Rollback Procedures

### Quick Rollback
```bash
# Rollback to previous version
kubectl rollout undo deployment/payments-service -n payments-platform
kubectl rollout undo deployment/risk-service -n payments-platform
kubectl rollout undo deployment/ledger-service -n payments-platform
```

### Payment Gateway Rollback
If payment processing issues occur:
1. Switch to backup payment gateway
2. Update gateway configuration
3. Monitor transaction success rates

## Post-Deployment Verification

1. **Health Checks**
   ```bash
   curl https://api.payments-platform.com/api/v1/payments/health
   curl https://api.payments-platform.com/api/v1/risk/health
   curl https://api.payments-platform.com/api/v1/ledger/health
   ```

2. **Payment Flow Tests**
   - Create payment intent
   - Process test payment
   - Verify ledger entry
   - Check risk evaluation

3. **Monitor Dashboards**
   - Payment success rate
   - Risk evaluation metrics
   - Fraud detection alerts
   - Transaction processing latency

## Common Issues and Resolution

### Issue: Payment Processing Failures
**Symptoms:** Payment success rate < 99%
**Resolution:**
1. Check payment gateway connectivity
2. Review gateway API responses
3. Check risk service status
4. Verify payment gateway credentials
5. Rollback if necessary

### Issue: High Risk Evaluation Latency
**Symptoms:** Risk evaluation > 200ms p95
**Resolution:**
1. Check Redis connectivity
2. Review risk rule complexity
3. Scale risk service instances
4. Optimize database queries

### Issue: Fraud Detection Issues
**Symptoms:** High false positive rate
**Resolution:**
1. Review risk rule thresholds
2. Check ML model performance
3. Adjust risk scoring weights
4. Monitor fraud metrics

## PCI DSS Compliance Checks

- [ ] All card data encrypted at rest
- [ ] All card data encrypted in transit
- [ ] Access logs reviewed
- [ ] Security events monitored
- [ ] Compliance status verified

## Emergency Contacts

- **On-Call Engineer:** [Contact Info]
- **Payment Operations:** [Contact Info]
- **Security Team:** [Contact Info]
- **Payment Gateway Support:** [Contact Info]

## Maintenance Windows

- **Scheduled:** Every Sunday 3-5 AM UTC
- **Emergency:** As needed with approval
- **PCI Compliance:** Quarterly security reviews


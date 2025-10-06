# Architecture Readiness Checklist

## 1. Domain Driven Design and Bounded Contexts
- [ ] Context map added describing domains and relationships
- [ ] Extraction seams defined for potential services
- [ ] Anti-corruption layer strategy documented for external integrations

## 2. Data Architecture and Consistency
- [ ] CQRS applied where read throughput or reporting requires it
- [ ] Idempotency keys enforced on all mutating APIs
- [ ] Data duplication policy documented (authoritative source per entity)

## 3. Integration and Versioning
- [ ] API versioning policy adopted (URI or header based)
- [ ] AsyncAPI or event schema catalog published
- [ ] Backward/forward compatibility rules documented for events and APIs

## 4. Reliability Patterns
- [ ] Timeouts, retries, and circuit breakers configured for all external calls
- [ ] Bulkheads and thread/connection pools sized with runbook guidance
- [ ] Outbox and idempotent consumers for all cross-boundary writes

## 5. Observability and SLOs
- [ ] Service level objectives defined with targets and alerts
- [ ] Centralized log shipping with correlation and tenant fields
- [ ] Golden signals dashboards for API, DB, cache, queues

## 6. Security and Policy
- [ ] Vault integrated for secrets and dynamic DB credentials
- [ ] mTLS between services enabled with rotation runbook
- [ ] Policy engine decisions externalized and audited

## 7. Platform Primitives
- [ ] Service mesh policies validated in cluster (mTLS, traffic splits)
- [ ] Gateway rate limits and quotas tested per tenant
- [ ] Discovery and health models verified under failure drills

## 8. Delivery and Operations
- [ ] Externalized config via config server with drift detection
- [ ] Progressive delivery pipeline (canary/blue green) established
- [ ] Infra as code for environments with automated drift remediation

## 9. Performance and Capacity
- [ ] Back pressure and load shedding at API and worker queues
- [ ] Capacity model with headroom policy and autoscaling thresholds
- [ ] Performance test suite with p95/p99 budgets enforced in CI

## 10. Data Governance
- [ ] Schema registry in place with version enforcement
- [ ] Retention, archival, and PII policies documented and automated
- [ ] Migration runbook with preflight checks and rollback strategy

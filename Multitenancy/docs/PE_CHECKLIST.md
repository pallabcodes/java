### Principal engineer review checklist

- Architecture: boundaries, idempotency, hedging semantics
- Reliability: SLOs, burn rollback, quotas sized
- Security: s2s JWT rotation, mTLS, OPA, secrets management
- Data: outbox/DLQ monitored, topic retention, key lifecycle
- Communication: Feign timeouts/bulkhead/CB, Kafka request-reply, gRPC health/auth
- Observability: RED metrics, traces with exemplars, dashboards
- CI/CD: Pact gates, secrets/license scans, canary rollback
- Operations: DR drill + evidence, chaos hooks, GitOps rules
- Kubernetes: Kyverno policy, HPA tuned, PDB/readiness
- Docs: comm patterns, runbooks, config keys, oncall quick start
- Compliance: SBOM plan, license inventory
- Open: RPO/RTO targets, multi-region replication, idempotency window

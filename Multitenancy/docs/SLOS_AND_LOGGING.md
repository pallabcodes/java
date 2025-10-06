# SLOs and Central Logging

## SLOs
- Availability target per API: 99.9%
- Latency targets: p95 < 200ms, p99 < 400ms
- Error budget policy documented in monitoring/alerts.yml

## Dashboards
- Golden signals for API, DB, cache, queues
- Per-tenant dashboards with rate limit and webhook outcomes

## Central logging
- Ship JSON logs to ELK or Loki
- Include correlation id and tenant id fields
- Redact secrets and PII per security policy

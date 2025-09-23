# SLA / Performance Baseline

Target scope: internal, small-scale multi-tenant.

- Latency (cold cache, p95):
  - List endpoints (projects/issues): < 120 ms
  - Search (/api/search/issues): < 150 ms for q-length ≤ 64
- Throughput: comfortably 200 rpm per tenant (aligned with rate limit), burst-tested to 500 rpm with acceptable p95 (< 220 ms)
- DB size assumptions:
  - Projects: ≤ 5k per tenant
  - Issues: ≤ 100k per tenant
- Resource footprint (container):
  - JVM: 256–512 MB heap (JAVA_OPTS default)
  - CPU: 0.5 vCPU baseline; scales linearly with concurrency

Test method (repeatable):
- Use Testcontainers Postgres with Flyway V1–V3
- Seed 10k issues per tenant; warm-up 1 minute; measure with wrk/k6
- Verify indexes in place (`pg_trgm`, JSONB GIN, partial/BRIN)

Notes:
- Search prioritizes title similarity over description (1.5x weight)
- Page size capped at 100; defaults to 20 to stabilize latency

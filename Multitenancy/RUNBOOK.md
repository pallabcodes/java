# Netflix Productivity Platform – Operational Runbook

## 1) Startup Checklist
- Environment
  - JAVA 17 available; container image `eclipse-temurin:17-jre` if Docker
  - Database reachable: `POSTGRESQL` host/port/creds
- Configuration
  - `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
  - Optional: `spring.security.jwt.secret`
- Flyway
  - Migrations enabled (`spring.flyway.enabled=true`)
  - On first boot, verify `V1__init_multitenant_schema.sql`, `V2__indexes_search_jsonb.sql`, `V3__search_case_insensitive.sql` applied
- Health
  - `GET /api/actuator/health` → `UP`
  - `GET /api/actuator/metrics` → returns metrics list

## 2) Tenant Safety Verification
- Attempt cross-tenant reads should fail (404/400)
  - Create issue under `tenantA`; fetch with `X-Tenant-ID: tenantB` should not return the entity
- Confirm Hibernate filter activated
  - Logs include MDC: `tenantId`, `requestId`

## 3) Common Commands
- Local run
  - `make run` or `mvn spring-boot:run`
- Tests
  - Unit: `make test`
  - Integration/E2E: `make itest`
- Migrations
  - `make migrate` (ensure DB env vars are set)
- Docker
  - `docker compose up -d --build`

## 4) Failure Handling
- Flyway migration error
  - Check `flyway_schema_history` for failed row; fix SQL; re-run migration (new version preferred)
  - Never modify applied scripts; add `Vx__fix.sql`
- Database unavailable
  - Validate network/security groups/firewall; check Postgres logs
- High latency
  - Run `EXPLAIN ANALYZE` for slow endpoints’ SQL
  - Verify trigram/JSONB/partial indexes exist: `\d+ issues` in psql
  - Ensure `pg_trgm` extension installed
- Memory pressure
  - Reduce cache size/TTL in `CacheConfig`
  - Adjust `JAVA_OPTS` (`-Xmx`)

## 5) Observability
- Logs include MDC fields: `requestId`, `tenantId`, `userId`, `role`
- Metrics to watch
  - `http.server.requests` (p95 latency)
  - Cache: expose simple counters/hit ratio (if enabled)
  - DB connection pool stats (Hikari)

## 6) Security & Rate Limiting
- Rate limit default: 200 rpm per tenant (Bucket4j)
- Security headers set by `SecurityHeadersConfig`
- JWT tenant extraction optional; if present, header and claim should match

## 7) Capacity & Tuning
- Page size capped at 100; default 20
- Search limit capped at 50; title boosted
- Consider BRIN reindex for large tables; regular `VACUUM (ANALYZE)`

## 8) DBA Quick Tips
- Extensions
  - `create extension if not exists pg_trgm;`
- Index bloat check (example)
  - `SELECT schemaname, relname, pg_size_pretty(pg_total_relation_size(relid)) FROM pg_stat_user_tables ORDER BY pg_total_relation_size(relid) DESC LIMIT 20;`
- Reindex (example)
  - `REINDEX INDEX CONCURRENTLY idx_issues_title_trgm;`

## 9) Incident Playbook
- Spike in 5xx
  - Check DB availability; examine slow queries; reduce page size if client abuse
- Spike in 429
  - Rate limit triggered; confirm tenant traffic; adjust policy if needed
- Data leakage suspicion
  - Verify tenant filter enabled; audit logs; run targeted queries by `tenant_id`

## 10) Change Management
- All schema changes via Flyway new versions only
- Document user‑visible API changes in OpenAPI; bump minor if response shape changes

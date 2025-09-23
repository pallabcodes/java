# Netflix Productivity Platform – System Design and Architecture Decisions

## 1) Why Modular Monolith
- Internal scope; development speed and operability prioritized over ceremony.
- Lowest infra cost: one deployable, one database; simple CI/CD.
- Clear modular boundaries inside the monolith enable future extraction without churn.

## 2) Bounded Contexts (Modules)
- core: bootstrapping, config, api envelope, exceptions, security, observability
- tenants: tenant context/resolution/interceptor; Hibernate Filter enforcement
- projects: entity, dto, mapper, repository, service, controller
- issues: entity, dto, mapper, repository, service, controller
- workflows (future): status machines, transitions, hooks/guards
- search: Postgres trigram + tsvector queries; JSONB awareness
- platform: caching, rate limiting, request shaping, metrics
- integrations (optional): mail/webhooks when needed

## 3) Data Strategy
- Single PostgreSQL instance; strict app-level tenant isolation:
  - Hibernate Filter per request: `tenant_id = :tenantId`
  - Composite unique constraints include `tenant_id`
- JSONB `custom_fields` for flexible extensibility without schema churn
- Search: `pg_trgm` (GIN trigram) and `to_tsvector` for fast, low-cost relevance

## 4) Multi-Tenancy Enforcement
- Resolution order: Header → Param → Path → Subdomain → JWT claim
- Per-request activation of Hibernate Filter in `TenantInterceptor`
- Repository queries scoped; constraints prevent cross-tenant collisions

## 5) Performance Strategy
- Indexes: GIN trigram for title/description, partial indexes for hot predicates, BRIN on time columns
- Caffeine caches (per-tenant keys) for hot reads; short TTL and caps
- Batch writes (JPA batch), payload minimization, HTTP compression enabled
- Avoid N+1: selective fetch joins and focused projections

## 6) Security
- Minimal JWT parsing: extract tenant/user claims and reconcile with request
- Per-tenant rate limiting via Bucket4j (default 200 rpm) to protect the app
- Bean Validation on DTOs; unified error envelope for clients

## 7) Observability
- MDC: `requestId`, `tenantId`, `userId`, `role` injected in interceptor
- Micrometer baseline with room for domain metrics (e.g., search latency)
- Consistent `ApiResponse` and global exception handler

## 8) API Standards
- OpenAPI enabled; versioned base path `/api`
- Consistent pagination, error codes, and response envelope (success, status, message, data)

## 9) Folder Structure
```
com.netflix.productivity
  core (config, api, exception)
  tenants (context, resolver, interceptor)
  projects (entity, dto, mapper, repo, service, controller)
  issues (entity, dto, mapper, repo, service, controller)
  search (service, controller)
  platform (cache, rate-limit)
```

## 10) Trade-offs and Non-Goals
- No Kafka/Elasticsearch/OpenSearch at this scale; Postgres-first approach
- No service mesh; monolith keeps cost/ops low and debugging simple
- JSONB beats table-per-custom-field churn; keep a few indexed JSON paths as needed

## 11) Future Levers
- PostgreSQL RLS if regulatory demands DB-level isolation in addition to app-level filter
- Extract `search` or `workflows` to separate services if growth mandates
- Add async pipelines/backpressure for heavy workloads (exports/bulk ops)

## 12) Reviewer Checklist
- Tenant isolation: Hibernate Filter enabled; constraints include tenant; no leakage
- Queries aligned with indexes; `EXPLAIN ANALYZE` shows index usage on hot paths
- Cache keys include tenant; invalidation occurs on write paths
- Errors consistent with `ApiResponse`; OpenAPI reflects shapes/versioning
- Tests: repository (tenant scope), controller E2E (headers/path/JWT), Flyway migrations V1/V2

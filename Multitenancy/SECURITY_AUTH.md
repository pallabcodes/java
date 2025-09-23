# Authentication & Authorization (Multi-Tenancy)

## Goals
- Fast, stateless JWT auth with per-tenant isolation
- Clear policy layer; easy to reason about and debug
- No overengineering; upgrade path to SSO/OIDC

## AuthN
- Login: `POST /api/auth/login` with `{ tenantId, username|email, password }`
- Password hashing: Argon2id
- JWT claims: `sub` (userId), `tenant`, `roles`, `ver` (token version), `iat`, `exp`
- Token rotation: supported via `ver` (increment to invalidate all tokens)

## AuthZ
- Enforcement layers:
  1) Tenant boundary: resolved from request and validated (must match token)
  2) Hibernate Filter (`tenant_id = :tenantId`) – prevents data leakage
  3) `@RequirePermission` + `PermissionAspect` – role/policy checks per action
- Roles/permissions (baseline):
  - `TENANT_ADMIN` – full access within tenant
  - `PROJECT_ADMIN`, `PROJECT_MEMBER`, `VIEWER` – mapped to `PROJECT_*`/`ISSUE_*` permissions

## Token Handling
- Header: `Authorization: Bearer <jwt>`
- Filter: extracts user/tenant/roles → `SecurityContext` and `TenantContext`
- Reconciliation: `X-Tenant-ID` (if present) must match token’s tenant

## Debuggability
- Decision logs: MDC includes `tenantId`, `userId`, `requestId`
- PermissionAspect throws `403` with code `AUTH_403_NO_PERMISSION:<perm>`
- Error codes catalog: see `API_ERROR_CODES.md`

## Performance
- No remote calls on hot path; all checks in memory
- Role/membership caches are small TTL Caffeine caches (optional)

## Extensibility
- Add OIDC: plug a `OidcAuthenticationController` that issues our first-party JWT after ID token validation
- Add fine-grained policies: extend `PermissionAspect` to consult per-project membership

## Endpoints
- `POST /api/auth/login` → `{ accessToken, tokenType, expiresInMs }`
- Protected resources require `Authorization` header; non-auth endpoints remain public (health, docs)

Data model overview (reference only)

Source of truth
- Flyway migrations under `src/main/resources/db/migration/`
  - V1__init_multitenant_schema.sql (projects, issues, base indexes)
  - V2__indexes_search_jsonb.sql (JSONB columns, GIN, BRIN, partial indexes)
  - V3__search_case_insensitive.sql (case insensitive trigram)
  - V4__auth_schema.sql (users, roles, user_roles)
 - V5__auth_tokens.sql (refresh tokens). Password reset tokens are Redis TTL, not DB.

Entities and invariants
- projects
  - unique per tenant: (tenant_id, key)
  - soft delete with deleted_at
  - JSONB custom_fields with targeted indexes (see V2)
- issues
  - unique per tenant: (tenant_id, key)
  - search optimized: trigram on lower(title), TSV on description (V2/V3)
  - JSONB custom_fields with targeted indexes (see V2)
- users
  - unique per tenant: (tenant_id, email)
  - token_version for JWT revocation
- roles
  - unique per tenant: (tenant_id, name)
- user_roles
  - unique: (tenant_id, user_id, role_id)

Tenant isolation
- Enforced by Hibernate filter and repository scoping
- Composite uniques per tenant prevent cross tenant collisions

Why no SQL mirror here
- Avoids drift from Flyway migrations
- Reviewers are pointed directly to executable, versioned DDL
 - Password reset tokens are stored in Redis with TTL; DB only for long lived refresh tokens



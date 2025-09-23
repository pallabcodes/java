# Optional Postgres Row-Level Security (RLS)

RLS is not enabled by default (app-level tenant filter suffices at our scale). If compliance requires DB-level isolation, enable RLS per table.

## Setup
```
ALTER TABLE projects ENABLE ROW LEVEL SECURITY;
ALTER TABLE issues ENABLE ROW LEVEL SECURITY;

CREATE POLICY projects_tenant_isolation ON projects
  USING (tenant_id = current_setting('app.tenant_id', true));

CREATE POLICY issues_tenant_isolation ON issues
  USING (tenant_id = current_setting('app.tenant_id', true));
```

In application, set tenant in session before queries (DataSource proxy / interceptor):
```
SET app.tenant_id = '<tenantId>';
```

Caveats:
- Requires careful connection/session management
- Slight overhead vs app-level filter

Recommendation:
- Keep off by default; enable only if mandated by policy

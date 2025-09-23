-- Enable necessary extensions
create extension if not exists pg_trgm;

-- JSONB custom fields
alter table projects add column if not exists custom_fields jsonb;
alter table issues add column if not exists custom_fields jsonb;

-- GIN indexes for trigram search
create index if not exists idx_issues_title_trgm on issues using gin (title gin_trgm_ops);
create index if not exists idx_issues_description_trgm on issues using gin (description gin_trgm_ops);

-- JSONB GIN indexes
create index if not exists idx_projects_custom_fields on projects using gin (custom_fields jsonb_path_ops);
create index if not exists idx_issues_custom_fields on issues using gin (custom_fields jsonb_path_ops);

-- Partial indexes for hot predicates
create index if not exists idx_issues_open_by_tenant on issues (created_at) where status in ('OPEN','IN_PROGRESS') and deleted_at is null;
create index if not exists idx_projects_active_by_tenant on projects (created_at) where deleted_at is null;

-- BRIN for time-range scans
create index if not exists brin_issues_created_at on issues using brin (created_at);
create index if not exists brin_projects_created_at on projects using brin (created_at);


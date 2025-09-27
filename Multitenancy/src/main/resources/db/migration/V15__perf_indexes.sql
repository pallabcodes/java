-- Enable pg_trgm extension for trigram search
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Issues common filters and lookups
CREATE INDEX IF NOT EXISTS idx_issues_tenant_key ON issues (tenant_id, key) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_issues_tenant_project ON issues (tenant_id, project_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_issues_tenant_status ON issues (tenant_id, status) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_issues_tenant_assignee ON issues (tenant_id, assignee_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_issues_created_at ON issues (tenant_id, created_at) WHERE deleted_at IS NULL;

-- Text search on title and description using trigram
CREATE INDEX IF NOT EXISTS gin_issues_title_trgm ON issues USING gin (title gin_trgm_ops);
CREATE INDEX IF NOT EXISTS gin_issues_description_trgm ON issues USING gin (description gin_trgm_ops);

-- Comments list by issue
CREATE INDEX IF NOT EXISTS idx_comments_tenant_issue_created ON comments (tenant_id, issue_id, created_at);

-- Attachments list by issue
CREATE INDEX IF NOT EXISTS idx_attachments_tenant_issue_created ON attachments (tenant_id, issue_id, created_at);


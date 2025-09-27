ALTER TABLE issues ADD COLUMN IF NOT EXISTS sla_breached_at TIMESTAMPTZ;
CREATE INDEX IF NOT EXISTS idx_issue_sla_breached ON issues (tenant_id, sla_breached_at) WHERE sla_breached_at IS NOT NULL;


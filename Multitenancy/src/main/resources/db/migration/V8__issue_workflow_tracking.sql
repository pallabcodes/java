-- Track workflow linkage on issues

ALTER TABLE issues
    ADD COLUMN IF NOT EXISTS workflow_id VARCHAR(36),
    ADD COLUMN IF NOT EXISTS workflow_state_id VARCHAR(36);

CREATE INDEX IF NOT EXISTS idx_issue_workflow ON issues(workflow_id);
CREATE INDEX IF NOT EXISTS idx_issue_workflow_state ON issues(workflow_state_id);



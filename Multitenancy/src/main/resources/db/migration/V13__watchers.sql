CREATE TABLE issue_watchers (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    issue_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    CONSTRAINT uq_issue_watcher UNIQUE (tenant_id, issue_id, user_id),
    CONSTRAINT fk_iw_issue FOREIGN KEY (tenant_id, issue_id) REFERENCES issues (tenant_id, id)
);

CREATE INDEX idx_watchers_issue ON issue_watchers (tenant_id, issue_id);


CREATE TABLE attachments (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    issue_id VARCHAR(36) NOT NULL,
    filename TEXT NOT NULL,
    content_type VARCHAR(255),
    size_bytes BIGINT NOT NULL,
    storage_key TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    deleted_at TIMESTAMPTZ,
    version BIGINT DEFAULT 0 NOT NULL,
    CONSTRAINT fk_attachment_issue FOREIGN KEY (tenant_id, issue_id) REFERENCES issues (tenant_id, id)
);

CREATE INDEX idx_attachments_tenant_issue ON attachments (tenant_id, issue_id);
CREATE INDEX idx_attachments_tenant_storage ON attachments (tenant_id, storage_key);


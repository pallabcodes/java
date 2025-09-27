CREATE TABLE labels (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    color VARCHAR(7),
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    deleted_at TIMESTAMPTZ,
    version BIGINT DEFAULT 0 NOT NULL,
    CONSTRAINT uq_label_name UNIQUE (tenant_id, name)
);

CREATE TABLE issue_labels (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    issue_id VARCHAR(36) NOT NULL,
    label_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    CONSTRAINT uq_issue_label UNIQUE (tenant_id, issue_id, label_id),
    CONSTRAINT fk_il_issue FOREIGN KEY (tenant_id, issue_id) REFERENCES issues (tenant_id, id),
    CONSTRAINT fk_il_label FOREIGN KEY (tenant_id, label_id) REFERENCES labels (tenant_id, id)
);

CREATE INDEX idx_labels_tenant ON labels (tenant_id);
CREATE INDEX idx_issue_labels_issue ON issue_labels (tenant_id, issue_id);


-- Attachments Service Database Schema

CREATE TABLE IF NOT EXISTS attachments (
    id                varchar(36) PRIMARY KEY,
    tenant_id         varchar(36) NOT NULL,
    issue_id          varchar(36) NOT NULL,
    filename          varchar(255) NOT NULL,
    content_type      varchar(255) NOT NULL,
    size_bytes        bigint NOT NULL,
    storage_key       varchar(512) NOT NULL,
    checksum_sha256   varchar(64),
    created_by        varchar(36) NOT NULL,
    created_at        timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_attachments_tenant_issue ON attachments(tenant_id, issue_id);
CREATE INDEX IF NOT EXISTS idx_attachments_created_at ON attachments(created_at);

CREATE TABLE IF NOT EXISTS upload_tokens (
    id                varchar(36) PRIMARY KEY,
    tenant_id         varchar(36) NOT NULL,
    issue_id          varchar(36) NOT NULL,
    filename          varchar(255) NOT NULL,
    content_type      varchar(255) NOT NULL,
    size_bytes        bigint NOT NULL,
    storage_key       varchar(512) NOT NULL,
    expires_at        timestamp NOT NULL,
    created_by        varchar(36) NOT NULL,
    created_at        timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_upload_tokens_tenant_issue ON upload_tokens(tenant_id, issue_id);
CREATE INDEX IF NOT EXISTS idx_upload_tokens_expires_at ON upload_tokens(expires_at);

-- Comments and audit events schema

CREATE TABLE IF NOT EXISTS comments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(50)  NOT NULL,
    project_id      VARCHAR(36)  NOT NULL,
    issue_id        VARCHAR(36)  NOT NULL,
    author_user_id  VARCHAR(36)  NOT NULL,
    body            TEXT         NOT NULL,
    is_deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    edited_at       TIMESTAMPTZ,
    version         BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_comment_tenant ON comments(tenant_id);
CREATE INDEX IF NOT EXISTS idx_comment_issue ON comments(issue_id);
CREATE INDEX IF NOT EXISTS idx_comment_project ON comments(project_id);
CREATE INDEX IF NOT EXISTS idx_comment_created ON comments(created_at);

CREATE TABLE IF NOT EXISTS audit_events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(50)  NOT NULL,
    project_id      VARCHAR(36),
    issue_id        VARCHAR(36),
    entity_type     VARCHAR(64)  NOT NULL,
    entity_id       VARCHAR(64)  NOT NULL,
    action          VARCHAR(64)  NOT NULL,
    actor_user_id   VARCHAR(36)  NOT NULL,
    message         VARCHAR(1000),
    metadata        JSONB,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_audit_tenant ON audit_events(tenant_id);
CREATE INDEX IF NOT EXISTS idx_audit_project ON audit_events(project_id);
CREATE INDEX IF NOT EXISTS idx_audit_issue ON audit_events(issue_id);
CREATE INDEX IF NOT EXISTS idx_audit_created ON audit_events(created_at);
CREATE INDEX IF NOT EXISTS idx_audit_entity ON audit_events(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_metadata_gin ON audit_events USING GIN (metadata);



CREATE TABLE webhooks (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    url TEXT NOT NULL,
    secret VARCHAR(128),
    enabled BOOLEAN DEFAULT TRUE NOT NULL,
    event_filter TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW() NOT NULL
);

CREATE UNIQUE INDEX uq_webhooks_tenant_name ON webhooks (tenant_id, name);

CREATE TABLE webhook_deliveries (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    webhook_id VARCHAR(36) NOT NULL,
    event_id VARCHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL,
    attempt INTEGER DEFAULT 0 NOT NULL,
    next_attempt_at TIMESTAMPTZ,
    response_status INTEGER,
    response_body TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    CONSTRAINT fk_whd_webhook FOREIGN KEY (tenant_id, webhook_id) REFERENCES webhooks (tenant_id, id)
);

CREATE INDEX idx_whd_tenant_next ON webhook_deliveries (tenant_id, next_attempt_at) WHERE status = 'PENDING';


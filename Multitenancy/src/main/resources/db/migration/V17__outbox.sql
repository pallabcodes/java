CREATE TABLE IF NOT EXISTS outbox_events (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    aggregate_type VARCHAR(128) NOT NULL,
    aggregate_id VARCHAR(128) NOT NULL,
    payload JSONB NOT NULL,
    headers JSONB,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    topic VARCHAR(256) NOT NULL,
    partition_key VARCHAR(256),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    available_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    sent_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_outbox_status_available ON outbox_events (status, available_at);
CREATE INDEX IF NOT EXISTS idx_outbox_tenant_created ON outbox_events (tenant_id, created_at);


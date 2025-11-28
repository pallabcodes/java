-- Event Store Tables for EDA
-- Provides append-only event storage with replay capabilities

-- Main event store table - append-only
CREATE TABLE IF NOT EXISTS event_store (
    -- Technical ID for database operations
    id BIGSERIAL PRIMARY KEY,

    -- Event identification
    event_id VARCHAR(255) NOT NULL UNIQUE,
    event_type VARCHAR(255) NOT NULL,

    -- Aggregate identification (for event sourcing)
    aggregate_id VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(255) NOT NULL,

    -- Event data (stored as JSONB for flexible schema evolution)
    event_data JSONB NOT NULL,

    -- Tracing and correlation
    correlation_id VARCHAR(255) NOT NULL,
    causation_id VARCHAR(255),

    -- Multi-tenancy support
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'default',

    -- Schema versioning for evolution
    schema_version VARCHAR(16) NOT NULL DEFAULT '1.0',

    -- Timestamps
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    -- Aggregate versioning (for optimistic concurrency)
    version BIGINT NOT NULL,

    -- Constraints
    CONSTRAINT uk_event_store_event_id UNIQUE (event_id),
    CONSTRAINT ck_event_store_version_positive CHECK (version >= 0)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_event_store_aggregate_id_version
    ON event_store (aggregate_id, version);

CREATE INDEX IF NOT EXISTS idx_event_store_aggregate_type
    ON event_store (aggregate_type);

CREATE INDEX IF NOT EXISTS idx_event_store_event_type
    ON event_store (event_type);

CREATE INDEX IF NOT EXISTS idx_event_store_created_at
    ON event_store (created_at);

CREATE INDEX IF NOT EXISTS idx_event_store_correlation_id
    ON event_store (correlation_id);

CREATE INDEX IF NOT EXISTS idx_event_store_tenant_id
    ON event_store (tenant_id);

-- Partial index for recent events (optimize for replay operations)
CREATE INDEX IF NOT EXISTS idx_event_store_recent
    ON event_store (created_at DESC)
    WHERE created_at > now() - interval '30 days';

-- Snapshot table for performance optimization
CREATE TABLE IF NOT EXISTS event_snapshots (
    id BIGSERIAL PRIMARY KEY,

    -- Aggregate identification
    aggregate_id VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(255) NOT NULL,

    -- Snapshot data
    snapshot_data JSONB NOT NULL,
    snapshot_version BIGINT NOT NULL,

    -- Metadata
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'default',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    -- Constraints
    CONSTRAINT uk_event_snapshots_aggregate UNIQUE (aggregate_id, aggregate_type)
);

-- Indexes for snapshots
CREATE INDEX IF NOT EXISTS idx_event_snapshots_aggregate
    ON event_snapshots (aggregate_id, aggregate_type);

-- Event replay tracking table (for monitoring replay operations)
CREATE TABLE IF NOT EXISTS event_replay_tracking (
    id BIGSERIAL PRIMARY KEY,

    replay_id VARCHAR(255) NOT NULL UNIQUE,
    replay_type VARCHAR(50) NOT NULL, -- FULL, AGGREGATE, TIME_BASED

    -- Replay parameters
    aggregate_id VARCHAR(255),
    event_type VARCHAR(255),
    from_time TIMESTAMPTZ,
    to_time TIMESTAMPTZ,

    -- Status tracking
    status VARCHAR(20) NOT NULL DEFAULT 'STARTED', -- STARTED, RUNNING, COMPLETED, FAILED
    events_processed BIGINT NOT NULL DEFAULT 0,
    started_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at TIMESTAMPTZ,
    error_message TEXT,

    -- Metadata
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'default',
    initiated_by VARCHAR(255),

    CONSTRAINT ck_replay_tracking_status
        CHECK (status IN ('STARTED', 'RUNNING', 'COMPLETED', 'FAILED'))
);

-- Indexes for replay tracking
CREATE INDEX IF NOT EXISTS idx_event_replay_tracking_status
    ON event_replay_tracking (status);

CREATE INDEX IF NOT EXISTS idx_event_replay_tracking_tenant
    ON event_replay_tracking (tenant_id);

-- Event deduplication table (for handling duplicate events from retries)
CREATE TABLE IF NOT EXISTS event_deduplication (
    event_id VARCHAR(255) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'default',
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    -- TTL for cleanup (events older than 7 days can be removed)
    CONSTRAINT ck_event_deduplication_recent
        CHECK (processed_at > now() - interval '7 days')
);

-- Event processing failure table (for dead letter queue)
CREATE TABLE IF NOT EXISTS event_processing_failures (
    id BIGSERIAL PRIMARY KEY,

    event_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    aggregate_id VARCHAR(255),
    aggregate_type VARCHAR(255),

    -- Failure details
    error_message TEXT NOT NULL,
    error_type VARCHAR(255) NOT NULL,
    stack_trace TEXT,

    -- Retry information
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retries INTEGER NOT NULL DEFAULT 3,
    next_retry_at TIMESTAMPTZ,

    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'FAILED', -- FAILED, RETRYING, DEAD_LETTER

    -- Metadata
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'default',
    correlation_id VARCHAR(255),
    processor_name VARCHAR(255),
    failed_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT ck_processing_failures_status
        CHECK (status IN ('FAILED', 'RETRYING', 'DEAD_LETTER'))
);

-- Indexes for failure handling
CREATE INDEX IF NOT EXISTS idx_event_processing_failures_status
    ON event_processing_failures (status);

CREATE INDEX IF NOT EXISTS idx_event_processing_failures_next_retry
    ON event_processing_failures (next_retry_at)
    WHERE next_retry_at IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_event_processing_failures_event
    ON event_processing_failures (event_id);

-- Comments for documentation
COMMENT ON TABLE event_store IS 'Append-only event store for domain events - core of EDA';
COMMENT ON TABLE event_snapshots IS 'Performance optimization for event-sourced aggregates';
COMMENT ON TABLE event_replay_tracking IS 'Monitoring and tracking of event replay operations';
COMMENT ON TABLE event_deduplication IS 'Duplicate event detection and prevention';
COMMENT ON TABLE event_processing_failures IS 'Event processing failures and retry tracking';

-- Grant permissions (adjust based on your security model)
-- GRANT SELECT, INSERT ON event_store TO event_processor;
-- GRANT SELECT, INSERT ON event_snapshots TO event_processor;
-- GRANT SELECT, INSERT, UPDATE ON event_processing_failures TO event_processor;
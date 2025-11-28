-- Playback Service Database Schema
-- CQRS Read Models and Transactional Outbox for Event-Driven Architecture

-- Transactional Outbox for reliable event publishing
CREATE TABLE IF NOT EXISTS playback_outbox_events (
    id BIGSERIAL PRIMARY KEY,

    -- Event identification
    event_id VARCHAR(255) NOT NULL UNIQUE,
    event_type VARCHAR(255) NOT NULL,

    -- Aggregate identification
    aggregate_id VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(255) NOT NULL,

    -- Event data (stored as JSONB for flexible schema)
    event_data JSONB NOT NULL,

    -- Tracing and correlation
    correlation_id VARCHAR(255) NOT NULL,
    causation_id VARCHAR(255),

    -- Multi-tenancy
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'default',

    -- Processing status
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retries INTEGER NOT NULL DEFAULT 3,

    -- Error handling
    error_message TEXT,
    next_retry_at TIMESTAMPTZ,

    -- Timestamps
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    available_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    sent_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uk_playback_outbox_event_id UNIQUE (event_id),
    CONSTRAINT ck_playback_outbox_status CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'DLQ'))
);

-- Indexes for outbox performance
CREATE INDEX IF NOT EXISTS idx_playback_outbox_status_available
    ON playback_outbox_events (status, available_at);

CREATE INDEX IF NOT EXISTS idx_playback_outbox_correlation
    ON playback_outbox_events (correlation_id);

CREATE INDEX IF NOT EXISTS idx_playback_outbox_aggregate
    ON playback_outbox_events (aggregate_id, aggregate_type);

CREATE INDEX IF NOT EXISTS idx_playback_outbox_retry
    ON playback_outbox_events (next_retry_at)
    WHERE next_retry_at IS NOT NULL;

-- CQRS Read Model: Playback Sessions
CREATE TABLE IF NOT EXISTS playback_sessions (
    session_id VARCHAR(255) PRIMARY KEY,

    -- Core session data
    user_id VARCHAR(255) NOT NULL,
    content_id VARCHAR(255) NOT NULL,
    content_type VARCHAR(50) NOT NULL,
    device_type VARCHAR(50) NOT NULL,
    quality VARCHAR(20) NOT NULL,

    -- Timing
    started_at TIMESTAMPTZ NOT NULL,
    last_activity_at TIMESTAMPTZ NOT NULL,

    -- Playback state
    current_position BIGINT,
    status VARCHAR(20) NOT NULL,
    total_watch_time BIGINT DEFAULT 0,
    quality_changes INTEGER DEFAULT 0,
    total_buffering_time BIGINT DEFAULT 0,
    final_quality VARCHAR(20),

    -- Computed fields
    completion_percentage DECIMAL(5,2),
    session_duration BIGINT, -- in milliseconds
    is_active BOOLEAN NOT NULL DEFAULT true,

    -- Metadata
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'default',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT ck_playback_sessions_status
        CHECK (status IN ('STARTED', 'PLAYING', 'PAUSED', 'BUFFERING', 'COMPLETED', 'INTERRUPTED')),
    CONSTRAINT ck_playback_sessions_completion
        CHECK (completion_percentage IS NULL OR (completion_percentage >= 0 AND completion_percentage <= 100))
);

-- Indexes for CQRS query performance
CREATE INDEX IF NOT EXISTS idx_playback_sessions_user_active
    ON playback_sessions (user_id, is_active)
    WHERE is_active = true;

CREATE INDEX IF NOT EXISTS idx_playback_sessions_content
    ON playback_sessions (content_id, tenant_id);

CREATE INDEX IF NOT EXISTS idx_playback_sessions_status
    ON playback_sessions (status, tenant_id);

CREATE INDEX IF NOT EXISTS idx_playback_sessions_started
    ON playback_sessions (started_at DESC);

CREATE INDEX IF NOT EXISTS idx_playback_sessions_tenant
    ON playback_sessions (tenant_id);

-- Partial index for active sessions (most queried)
CREATE INDEX IF NOT EXISTS idx_playback_sessions_active_recent
    ON playback_sessions (last_activity_at DESC)
    WHERE is_active = true;

-- CQRS Read Model: User Playback Statistics
CREATE TABLE IF NOT EXISTS user_playback_stats (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'default',

    -- Aggregated statistics
    total_sessions BIGINT NOT NULL DEFAULT 0,
    total_watch_time BIGINT NOT NULL DEFAULT 0, -- in milliseconds
    avg_session_duration BIGINT, -- in milliseconds
    completion_rate DECIMAL(5,2), -- percentage

    -- Content preferences
    favorite_genre VARCHAR(100),
    favorite_content_type VARCHAR(50),

    -- Device preferences
    preferred_device VARCHAR(50),
    preferred_quality VARCHAR(20),

    -- Activity metrics
    last_session_at TIMESTAMPTZ,
    sessions_this_week INTEGER DEFAULT 0,
    sessions_this_month INTEGER DEFAULT 0,

    -- Metadata
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uk_user_playback_stats_user UNIQUE (user_id, tenant_id)
);

-- Indexes for user stats queries
CREATE INDEX IF NOT EXISTS idx_user_playback_stats_user
    ON user_playback_stats (user_id, tenant_id);

CREATE INDEX IF NOT EXISTS idx_user_playback_stats_last_session
    ON user_playback_stats (last_session_at DESC);

-- CQRS Read Model: Content Playback Statistics
CREATE TABLE IF NOT EXISTS content_playback_stats (
    id BIGSERIAL PRIMARY KEY,
    content_id VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'default',

    -- Aggregated viewing data
    total_views BIGINT NOT NULL DEFAULT 0,
    unique_viewers BIGINT NOT NULL DEFAULT 0,
    total_watch_time BIGINT NOT NULL DEFAULT 0, -- in milliseconds

    -- Completion metrics
    completed_views BIGINT NOT NULL DEFAULT 0,
    avg_completion_percentage DECIMAL(5,2),

    -- Quality metrics
    avg_buffering_time BIGINT, -- in milliseconds
    quality_distribution JSONB, -- { "720p": 100, "1080p": 250 }

    -- Geographic distribution
    top_regions JSONB, -- { "us-east-1": 150, "eu-west-1": 120 }

    -- Time-based metrics
    views_last_24h BIGINT DEFAULT 0,
    views_last_7d BIGINT DEFAULT 0,
    views_last_30d BIGINT DEFAULT 0,

    -- Engagement score (0-100)
    engagement_score DECIMAL(5,2),

    -- Metadata
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uk_content_playback_stats_content UNIQUE (content_id, tenant_id)
);

-- Indexes for content stats queries
CREATE INDEX IF NOT EXISTS idx_content_playback_stats_content
    ON content_playback_stats (content_id, tenant_id);

CREATE INDEX IF NOT EXISTS idx_content_playback_stats_engagement
    ON content_playback_stats (engagement_score DESC);

CREATE INDEX IF NOT EXISTS idx_content_playback_stats_views
    ON content_playback_stats (total_views DESC);

-- Event replay tracking table (for analytics and debugging)
CREATE TABLE IF NOT EXISTS playback_event_replay (
    id BIGSERIAL PRIMARY KEY,
    replay_id VARCHAR(255) NOT NULL UNIQUE,
    replay_type VARCHAR(50) NOT NULL, -- USER_SESSIONS, CONTENT_STATS, FULL_REPLAY

    -- Replay parameters
    user_id VARCHAR(255),
    content_id VARCHAR(255),
    from_date TIMESTAMPTZ,
    to_date TIMESTAMPTZ,

    -- Progress tracking
    events_processed BIGINT NOT NULL DEFAULT 0,
    projections_updated BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'STARTED',

    -- Timing
    started_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at TIMESTAMPTZ,

    -- Error handling
    error_message TEXT,

    -- Metadata
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'default',
    initiated_by VARCHAR(255),

    CONSTRAINT ck_replay_status CHECK (status IN ('STARTED', 'RUNNING', 'COMPLETED', 'FAILED'))
);

-- Indexes for replay tracking
CREATE INDEX IF NOT EXISTS idx_playback_event_replay_status
    ON playback_event_replay (status);

CREATE INDEX IF NOT EXISTS idx_playback_event_replay_tenant
    ON playback_event_replay (tenant_id);

-- Comments for documentation
COMMENT ON TABLE playback_outbox_events IS 'Transactional outbox for reliable event publishing in CQRS command side';
COMMENT ON TABLE playback_sessions IS 'CQRS read model for playback session queries';
COMMENT ON TABLE user_playback_stats IS 'CQRS read model for user-centric playback analytics';
COMMENT ON TABLE content_playback_stats IS 'CQRS read model for content-centric playback analytics';
COMMENT ON TABLE playback_event_replay IS 'Tracking for event replay operations and analytics recalculation';

-- Create a view for active session monitoring
CREATE OR REPLACE VIEW active_sessions_view AS
SELECT
    session_id,
    user_id,
    content_id,
    device_type,
    quality,
    started_at,
    last_activity_at,
    current_position,
    status,
    EXTRACT(EPOCH FROM (now() - last_activity_at)) * 1000 as idle_time_ms
FROM playback_sessions
WHERE is_active = true
  AND last_activity_at > now() - INTERVAL '1 hour'; -- Consider active if activity in last hour

-- Grant permissions (adjust based on your security model)
-- GRANT SELECT, INSERT, UPDATE ON playback_outbox_events TO playback_service;
-- GRANT SELECT, INSERT, UPDATE ON playback_sessions TO playback_service;
-- GRANT SELECT, INSERT, UPDATE ON user_playback_stats TO playback_service;
-- GRANT SELECT, INSERT, UPDATE ON content_playback_stats TO playback_service;
-- Analytics Service Database Schema
-- CQRS Read Models for Event-Driven Analytics

-- User Analytics Read Model (CQRS Query Side)
CREATE TABLE IF NOT EXISTS user_analytics (
    user_id VARCHAR(255) PRIMARY KEY,

    -- Engagement metrics
    total_sessions BIGINT NOT NULL DEFAULT 0,
    completed_sessions BIGINT NOT NULL DEFAULT 0,
    total_watch_time BIGINT NOT NULL DEFAULT 0, -- milliseconds
    completion_rate DECIMAL(5,2) DEFAULT 0.0,
    engagement_score DECIMAL(5,2) DEFAULT 0.0,

    -- Activity tracking
    active_sessions INTEGER DEFAULT 0,
    last_session_at TIMESTAMPTZ,
    first_session_at TIMESTAMPTZ,
    last_activity_at TIMESTAMPTZ,

    -- Preferences (stored as JSONB for flexibility)
    device_preferences JSONB DEFAULT '{}',
    quality_preferences JSONB DEFAULT '{}',
    content_type_preferences JSONB DEFAULT '{}',

    -- Behavioral insights
    consecutive_day_streak INTEGER DEFAULT 0,
    preferred_device VARCHAR(50),
    preferred_quality VARCHAR(20),

    -- Metadata
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'default',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT ck_user_analytics_completion_rate
        CHECK (completion_rate IS NULL OR (completion_rate >= 0 AND completion_rate <= 100)),
    CONSTRAINT ck_user_analytics_engagement_score
        CHECK (engagement_score IS NULL OR (engagement_score >= 0 AND engagement_score <= 100))
);

-- Content Analytics Read Model (CQRS Query Side)
CREATE TABLE IF NOT EXISTS content_analytics (
    content_id VARCHAR(255) PRIMARY KEY,

    -- Viewership metrics
    total_views BIGINT NOT NULL DEFAULT 0,
    unique_viewers BIGINT NOT NULL DEFAULT 0,
    completed_views BIGINT NOT NULL DEFAULT 0,
    total_watch_time BIGINT NOT NULL DEFAULT 0, -- milliseconds

    -- Quality metrics
    avg_completion_percentage DECIMAL(5,2) DEFAULT 0.0,
    avg_buffering_time BIGINT DEFAULT 0, -- milliseconds
    quality_changes BIGINT DEFAULT 0,

    -- Engagement score (0-100)
    engagement_score DECIMAL(5,2) DEFAULT 0.0,

    -- Distribution analytics (stored as JSONB)
    device_distribution JSONB DEFAULT '{}',
    quality_distribution JSONB DEFAULT '{}',
    geographic_distribution JSONB DEFAULT '{}',
    drop_off_points JSONB DEFAULT '{}', -- position -> drop-off count

    -- Time-based metrics
    views_last_24h BIGINT DEFAULT 0,
    views_last_7d BIGINT DEFAULT 0,
    views_last_30d BIGINT DEFAULT 0,

    -- Behavioral insights
    avg_session_duration BIGINT, -- milliseconds
    peak_viewing_hour INTEGER,
    user_retention_by_day JSONB DEFAULT '{}',

    -- Metadata
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'default',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT ck_content_analytics_completion
        CHECK (avg_completion_percentage IS NULL OR (avg_completion_percentage >= 0 AND avg_completion_percentage <= 100)),
    CONSTRAINT ck_content_analytics_engagement
        CHECK (engagement_score IS NULL OR (engagement_score >= 0 AND engagement_score <= 100))
);

-- Real-time Metrics Cache Table (for persistence and recovery)
CREATE TABLE IF NOT EXISTS realtime_metrics_cache (
    metric_key VARCHAR(255) PRIMARY KEY,
    metric_value TEXT NOT NULL, -- JSON or numeric value
    metric_type VARCHAR(50) NOT NULL, -- counter, gauge, histogram
    last_updated TIMESTAMPTZ NOT NULL DEFAULT now(),

    -- TTL for cleanup (automatically remove old metrics)
    expires_at TIMESTAMPTZ NOT NULL DEFAULT (now() + interval '5 minutes'),

    tenant_id VARCHAR(64) NOT NULL DEFAULT 'default'
);

-- Dashboard Subscription Tracking
CREATE TABLE IF NOT EXISTS dashboard_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL,
    subscription_type VARCHAR(50) NOT NULL, -- global, content_specific
    content_id VARCHAR(255), -- null for global subscriptions
    subscribed_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    tenant_id VARCHAR(64) NOT NULL DEFAULT 'default',
    user_id VARCHAR(255),

    CONSTRAINT uk_dashboard_subscriptions_session UNIQUE (session_id, subscription_type, content_id)
);

-- Analytics Event Processing Log (for debugging and monitoring)
CREATE TABLE IF NOT EXISTS analytics_processing_log (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    processing_status VARCHAR(20) NOT NULL, -- SUCCESS, FAILED, RETRY
    processing_start TIMESTAMPTZ NOT NULL DEFAULT now(),
    processing_end TIMESTAMPTZ,
    processing_duration_ms BIGINT,
    error_message TEXT,

    -- Event routing info
    topic_name VARCHAR(255),
    partition_id INTEGER,
    offset_value BIGINT,

    tenant_id VARCHAR(64) NOT NULL DEFAULT 'default',
    correlation_id VARCHAR(255),

    CONSTRAINT ck_processing_log_status CHECK (processing_status IN ('SUCCESS', 'FAILED', 'RETRY'))
);

-- Indexes for performance

-- User analytics indexes
CREATE INDEX IF NOT EXISTS idx_user_analytics_tenant_active
    ON user_analytics (tenant_id, last_activity_at DESC)
    WHERE active_sessions > 0;

CREATE INDEX IF NOT EXISTS idx_user_analytics_engagement
    ON user_analytics (tenant_id, engagement_score DESC);

CREATE INDEX IF NOT EXISTS idx_user_analytics_last_session
    ON user_analytics (tenant_id, last_session_at DESC);

-- Content analytics indexes
CREATE INDEX IF NOT EXISTS idx_content_analytics_tenant_views
    ON content_analytics (tenant_id, total_views DESC);

CREATE INDEX IF NOT EXISTS idx_content_analytics_engagement
    ON content_analytics (tenant_id, engagement_score DESC);

CREATE INDEX IF NOT EXISTS idx_content_analytics_recent_views
    ON content_analytics (tenant_id, views_last_24h DESC);

-- Real-time metrics indexes
CREATE INDEX IF NOT EXISTS idx_realtime_metrics_expires
    ON realtime_metrics_cache (expires_at);

CREATE INDEX IF NOT EXISTS idx_realtime_metrics_tenant
    ON realtime_metrics_cache (tenant_id, metric_type);

-- Dashboard subscriptions indexes
CREATE INDEX IF NOT EXISTS idx_dashboard_subscriptions_session
    ON dashboard_subscriptions (session_id);

CREATE INDEX IF NOT EXISTS idx_dashboard_subscriptions_content
    ON dashboard_subscriptions (content_id)
    WHERE content_id IS NOT NULL;

-- Processing log indexes
CREATE INDEX IF NOT EXISTS idx_processing_log_status_time
    ON analytics_processing_log (processing_status, processing_start DESC);

CREATE INDEX IF NOT EXISTS idx_processing_log_event
    ON analytics_processing_log (event_id);

CREATE INDEX IF NOT EXISTS idx_processing_log_correlation
    ON analytics_processing_log (correlation_id);

-- Create views for common analytics queries

-- Top performing content view
CREATE OR REPLACE VIEW top_content_view AS
SELECT
    content_id,
    total_views,
    unique_viewers,
    avg_completion_percentage,
    engagement_score,
    views_last_24h,
    ROW_NUMBER() OVER (ORDER BY engagement_score DESC) as ranking
FROM content_analytics
WHERE tenant_id = 'default'  -- Parameterize in application
ORDER BY engagement_score DESC
LIMIT 100;

-- Active user engagement view
CREATE OR REPLACE VIEW user_engagement_view AS
SELECT
    user_id,
    engagement_score,
    completion_rate,
    total_sessions,
    total_watch_time,
    last_activity_at,
    CASE
        WHEN last_activity_at > now() - interval '1 hour' THEN 'VERY_ACTIVE'
        WHEN last_activity_at > now() - interval '24 hours' THEN 'ACTIVE'
        WHEN last_activity_at > now() - interval '7 days' THEN 'RECENT'
        ELSE 'INACTIVE'
    END as activity_status
FROM user_analytics
WHERE tenant_id = 'default'  -- Parameterize in application
ORDER BY engagement_score DESC;

-- Comments for documentation
COMMENT ON TABLE user_analytics IS 'CQRS read model for user-centric analytics built from event streams';
COMMENT ON TABLE content_analytics IS 'CQRS read model for content-centric analytics built from event streams';
COMMENT ON TABLE realtime_metrics_cache IS 'Ephemeral storage for real-time metrics with TTL';
COMMENT ON TABLE dashboard_subscriptions IS 'Tracking of WebSocket dashboard subscriptions';
COMMENT ON TABLE analytics_processing_log IS 'Audit log for event processing operations';

-- Create a cleanup function for expired metrics
CREATE OR REPLACE FUNCTION cleanup_expired_metrics()
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM realtime_metrics_cache
    WHERE expires_at < now();

    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- Grant permissions (adjust based on your security model)
-- GRANT SELECT, INSERT, UPDATE ON user_analytics TO analytics_service;
-- GRANT SELECT, INSERT, UPDATE ON content_analytics TO analytics_service;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON realtime_metrics_cache TO analytics_service;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON dashboard_subscriptions TO analytics_service;
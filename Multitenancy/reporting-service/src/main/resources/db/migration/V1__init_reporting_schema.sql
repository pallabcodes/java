-- Reporting Service Database Schema
-- This service will have its own database for reporting-specific data

-- Create reporting database (this would be done by DBA in production)
-- CREATE DATABASE reporting_db;

-- Reporting-specific tables for analytics and caching
CREATE TABLE IF NOT EXISTS report_cache (
    id              varchar(36)  PRIMARY KEY,
    tenant_id       varchar(36)  NOT NULL,
    cache_key       varchar(255) NOT NULL,
    report_data     jsonb        NOT NULL,
    created_at      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at      timestamp    NOT NULL,
    created_by      varchar(36)  NOT NULL,
    
    CONSTRAINT uk_report_cache_tenant_key UNIQUE (tenant_id, cache_key)
);

-- Indexes for report cache
CREATE INDEX IF NOT EXISTS idx_report_cache_tenant_id ON report_cache(tenant_id);
CREATE INDEX IF NOT EXISTS idx_report_cache_expires_at ON report_cache(expires_at);
CREATE INDEX IF NOT EXISTS idx_report_cache_created_at ON report_cache(created_at);

-- Reporting metrics aggregation table for performance
CREATE TABLE IF NOT EXISTS report_metrics_agg (
    id              varchar(36)  PRIMARY KEY,
    tenant_id       varchar(36)  NOT NULL,
    project_id      varchar(36),
    metric_type     varchar(50)  NOT NULL,
    metric_name     varchar(100) NOT NULL,
    metric_value    decimal(15,4) NOT NULL,
    aggregation_period varchar(20) NOT NULL, -- daily, weekly, monthly
    period_start    timestamp    NOT NULL,
    period_end      timestamp    NOT NULL,
    created_at      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_report_metrics_agg UNIQUE (tenant_id, project_id, metric_type, metric_name, aggregation_period, period_start)
);

-- Indexes for metrics aggregation
CREATE INDEX IF NOT EXISTS idx_report_metrics_agg_tenant_id ON report_metrics_agg(tenant_id);
CREATE INDEX IF NOT EXISTS idx_report_metrics_agg_project_id ON report_metrics_agg(project_id);
CREATE INDEX IF NOT EXISTS idx_report_metrics_agg_metric_type ON report_metrics_agg(metric_type);
CREATE INDEX IF NOT EXISTS idx_report_metrics_agg_period ON report_metrics_agg(period_start, period_end);

-- Report templates for reusable report configurations
CREATE TABLE IF NOT EXISTS report_templates (
    id              varchar(36)  PRIMARY KEY,
    tenant_id       varchar(36)  NOT NULL,
    name            varchar(255) NOT NULL,
    description     text,
    template_config jsonb        NOT NULL,
    is_public       boolean      NOT NULL DEFAULT false,
    created_by      varchar(36)  NOT NULL,
    created_at      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_report_templates_tenant_name UNIQUE (tenant_id, name)
);

-- Indexes for report templates
CREATE INDEX IF NOT EXISTS idx_report_templates_tenant_id ON report_templates(tenant_id);
CREATE INDEX IF NOT EXISTS idx_report_templates_created_by ON report_templates(created_by);
CREATE INDEX IF NOT EXISTS idx_report_templates_is_public ON report_templates(is_public);

-- Report subscriptions for scheduled reports
CREATE TABLE IF NOT EXISTS report_subscriptions (
    id              varchar(36)  PRIMARY KEY,
    tenant_id       varchar(36)  NOT NULL,
    template_id     varchar(36)  NOT NULL,
    subscriber_email varchar(255) NOT NULL,
    schedule_cron   varchar(100) NOT NULL,
    is_active       boolean      NOT NULL DEFAULT true,
    last_sent_at    timestamp,
    created_at      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_report_subscriptions_template 
        FOREIGN KEY (template_id) REFERENCES report_templates(id) ON DELETE CASCADE
);

-- Indexes for report subscriptions
CREATE INDEX IF NOT EXISTS idx_report_subscriptions_tenant_id ON report_subscriptions(tenant_id);
CREATE INDEX IF NOT EXISTS idx_report_subscriptions_template_id ON report_subscriptions(template_id);
CREATE INDEX IF NOT EXISTS idx_report_subscriptions_schedule ON report_subscriptions(schedule_cron, is_active);

-- Report execution logs for audit and debugging
CREATE TABLE IF NOT EXISTS report_execution_logs (
    id              varchar(36)  PRIMARY KEY,
    tenant_id       varchar(36)  NOT NULL,
    report_type     varchar(50)  NOT NULL,
    execution_time_ms bigint     NOT NULL,
    status          varchar(20)  NOT NULL, -- success, error, timeout
    error_message   text,
    parameters      jsonb,
    created_at      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for execution logs
CREATE INDEX IF NOT EXISTS idx_report_execution_logs_tenant_id ON report_execution_logs(tenant_id);
CREATE INDEX IF NOT EXISTS idx_report_execution_logs_created_at ON report_execution_logs(created_at);
CREATE INDEX IF NOT EXISTS idx_report_execution_logs_status ON report_execution_logs(status);

-- Comments for documentation
COMMENT ON TABLE report_cache IS 'Cached report data for performance optimization';
COMMENT ON TABLE report_metrics_agg IS 'Pre-aggregated metrics for faster report generation';
COMMENT ON TABLE report_templates IS 'Reusable report configuration templates';
COMMENT ON TABLE report_subscriptions IS 'Scheduled report subscriptions for users';
COMMENT ON TABLE report_execution_logs IS 'Audit log of report executions for monitoring and debugging';

-- ML Pipeline Service Database Schema
-- Stores metadata for saga-based ML pipeline orchestration

-- Pipeline execution tracking
CREATE TABLE IF NOT EXISTS ml_pipeline_executions (
    execution_id VARCHAR(255) PRIMARY KEY,
    pipeline_type VARCHAR(50) NOT NULL, -- FEATURE_ENGINEERING, MODEL_TRAINING, MODEL_DEPLOYMENT, COMPLETE
    workflow_id VARCHAR(255) UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'STARTED', -- STARTED, RUNNING, COMPLETED, FAILED, CANCELLED

    -- Pipeline parameters
    pipeline_config JSONB,
    input_parameters JSONB,

    -- Execution tracking
    started_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at TIMESTAMPTZ,
    duration_ms BIGINT,

    -- Results and artifacts
    output_artifacts JSONB,
    metrics JSONB,

    -- Error handling
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,

    -- Metadata
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'default',
    correlation_id VARCHAR(255),
    created_by VARCHAR(255),

    CONSTRAINT ck_pipeline_executions_status CHECK (status IN ('STARTED', 'RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED')),
    CONSTRAINT ck_pipeline_executions_type CHECK (pipeline_type IN ('FEATURE_ENGINEERING', 'MODEL_TRAINING', 'MODEL_DEPLOYMENT', 'COMPLETE'))
);

-- Feature store metadata
CREATE TABLE IF NOT EXISTS feature_sets (
    feature_set_id VARCHAR(255) PRIMARY KEY,
    pipeline_execution_id VARCHAR(255) REFERENCES ml_pipeline_executions(execution_id),

    -- Feature metadata
    feature_count INTEGER NOT NULL,
    data_source VARCHAR(255) NOT NULL,
    feature_names TEXT[], -- Array of feature names
    feature_types JSONB, -- Feature name -> type mapping

    -- Storage information
    storage_path VARCHAR(500),
    storage_format VARCHAR(50), -- PARQUET, CSV, etc.
    file_size_bytes BIGINT,

    -- Quality metrics
    data_quality_score DECIMAL(3,2), -- 0.00 to 1.00
    null_value_percentage DECIMAL(5,2),

    -- Lifecycle
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ,
    is_active BOOLEAN NOT NULL DEFAULT true,

    -- Metadata
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'default',
    tags JSONB DEFAULT '{}'
);

-- Model metadata
CREATE TABLE IF NOT EXISTS ml_models (
    model_id VARCHAR(255) PRIMARY KEY,
    model_version VARCHAR(50) NOT NULL,
    pipeline_execution_id VARCHAR(255) REFERENCES ml_pipeline_executions(execution_id),

    -- Model characteristics
    model_type VARCHAR(100) NOT NULL, -- LINEAR_REGRESSION, NEURAL_NETWORK, etc.
    algorithm VARCHAR(100) NOT NULL,
    framework VARCHAR(100) NOT NULL, -- TENSORFLOW, PYTORCH, DL4J, etc.

    -- Training metadata
    training_data_size BIGINT,
    feature_count INTEGER,
    training_duration_ms BIGINT,
    training_config JSONB,

    -- Performance metrics
    accuracy DECIMAL(5,4),
    precision DECIMAL(5,4),
    recall DECIMAL(5,4),
    f1_score DECIMAL(5,4),
    custom_metrics JSONB,

    -- Storage
    model_path VARCHAR(500),
    model_size_bytes BIGINT,
    model_format VARCHAR(50), -- H5, PB, ONNX, etc.

    -- Lifecycle
    status VARCHAR(20) NOT NULL DEFAULT 'TRAINING', -- TRAINING, TRAINED, VALIDATING, DEPLOYED, RETIRED
    deployed_at TIMESTAMPTZ,
    retired_at TIMESTAMPTZ,

    -- Governance
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'default',
    created_by VARCHAR(255),
    approved_by VARCHAR(255),
    approval_date TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT ck_ml_models_status CHECK (status IN ('TRAINING', 'TRAINED', 'VALIDATING', 'DEPLOYED', 'RETIRED'))
);

-- Experiment tracking (A/B tests, model comparisons)
CREATE TABLE IF NOT EXISTS ml_experiments (
    experiment_id VARCHAR(255) PRIMARY KEY,
    experiment_name VARCHAR(255) NOT NULL,
    description TEXT,

    -- Experiment setup
    model_a_id VARCHAR(255) REFERENCES ml_models(model_id),
    model_b_id VARCHAR(255) REFERENCES ml_models(model_id),
    traffic_split DECIMAL(3,2) NOT NULL DEFAULT 0.5, -- 50/50 split by default

    -- Experiment parameters
    target_metric VARCHAR(100) NOT NULL, -- accuracy, engagement, etc.
    minimum_sample_size BIGINT,
    confidence_level DECIMAL(3,2) DEFAULT 0.95,

    -- Results
    status VARCHAR(20) NOT NULL DEFAULT 'RUNNING', -- RUNNING, COMPLETED, CANCELLED
    winner_model_id VARCHAR(255),
    statistical_significance DECIMAL(5,4),
    effect_size DECIMAL(5,4),

    -- Timing
    started_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at TIMESTAMPTZ,
    duration_days INTEGER,

    -- Metadata
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'default',
    created_by VARCHAR(255),
    tags JSONB DEFAULT '{}'
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_ml_pipeline_executions_status_created
    ON ml_pipeline_executions (status, started_at DESC);

CREATE INDEX IF NOT EXISTS idx_ml_pipeline_executions_correlation
    ON ml_pipeline_executions (correlation_id);

CREATE INDEX IF NOT EXISTS idx_feature_sets_active_expires
    ON feature_sets (is_active, expires_at)
    WHERE is_active = true;

CREATE INDEX IF NOT EXISTS idx_ml_models_status_created
    ON ml_models (status, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_ml_models_type
    ON ml_models (model_type, status);

CREATE INDEX IF NOT EXISTS idx_ml_experiments_status
    ON ml_experiments (status, started_at DESC);

-- Comments for documentation
COMMENT ON TABLE ml_pipeline_executions IS 'Tracks execution of ML pipeline sagas with Temporal workflows';
COMMENT ON TABLE feature_sets IS 'Metadata for engineered feature sets stored in feature store';
COMMENT ON TABLE ml_models IS 'Model registry with training metadata and performance metrics';
COMMENT ON TABLE ml_experiments IS 'A/B testing and model comparison experiments';

-- Create views for common queries
CREATE OR REPLACE VIEW active_ml_pipelines AS
SELECT
    execution_id,
    pipeline_type,
    status,
    started_at,
    EXTRACT(EPOCH FROM (now() - started_at)) * 1000 as duration_ms
FROM ml_pipeline_executions
WHERE status IN ('STARTED', 'RUNNING')
ORDER BY started_at DESC;

CREATE OR REPLACE VIEW model_performance_summary AS
SELECT
    model_type,
    COUNT(*) as model_count,
    AVG(accuracy) as avg_accuracy,
    MAX(accuracy) as best_accuracy,
    AVG(training_duration_ms) / 1000 as avg_training_seconds
FROM ml_models
WHERE status = 'DEPLOYED'
GROUP BY model_type
ORDER BY model_count DESC;
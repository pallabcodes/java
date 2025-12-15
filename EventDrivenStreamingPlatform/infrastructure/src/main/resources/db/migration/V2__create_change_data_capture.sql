-- Change Data Capture (CDC) Change Log Table
-- Captures database changes for event publishing

CREATE TABLE IF NOT EXISTS database_change_log (
    id BIGSERIAL PRIMARY KEY,
    
    -- Change identification
    table_name VARCHAR(255) NOT NULL,
    operation VARCHAR(20) NOT NULL CHECK (operation IN ('INSERT', 'UPDATE', 'DELETE')),
    primary_key VARCHAR(255) NOT NULL,
    
    -- Change data
    before_state JSONB,
    after_state JSONB,
    changed_columns TEXT[], -- Array of changed column names
    
    -- Transaction tracking
    transaction_id VARCHAR(255) NOT NULL,
    changed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Processing status
    processed_at TIMESTAMPTZ,
    failed_at TIMESTAMPTZ,
    error_message TEXT,
    
    -- Indexes for efficient querying
    CONSTRAINT ck_change_log_operation CHECK (operation IN ('INSERT', 'UPDATE', 'DELETE'))
);

-- Indexes for CDC processing
CREATE INDEX IF NOT EXISTS idx_change_log_pending 
    ON database_change_log (processed_at, failed_at) 
    WHERE processed_at IS NULL AND failed_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_change_log_table_operation 
    ON database_change_log (table_name, operation);

CREATE INDEX IF NOT EXISTS idx_change_log_changed_at 
    ON database_change_log (changed_at);

CREATE INDEX IF NOT EXISTS idx_change_log_transaction 
    ON database_change_log (transaction_id);

-- Partition by table name for large-scale deployments (optional)
-- CREATE TABLE database_change_log_playback PARTITION OF database_change_log
--     FOR VALUES WITH (MODULUS 4, REMAINDER 0);

-- Comments for documentation
COMMENT ON TABLE database_change_log IS 'Change log for Change Data Capture (CDC) - captures all database changes for event publishing';
COMMENT ON COLUMN database_change_log.before_state IS 'State before change (for UPDATE/DELETE)';
COMMENT ON COLUMN database_change_log.after_state IS 'State after change (for INSERT/UPDATE)';
COMMENT ON COLUMN database_change_log.changed_columns IS 'Array of column names that changed (for UPDATE)';
COMMENT ON COLUMN database_change_log.transaction_id IS 'Transaction ID for grouping related changes';


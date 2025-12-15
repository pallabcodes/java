-- Change Data Capture (CDC) Database Triggers
-- Automatically capture database changes to change log table

-- Function to capture changes
CREATE OR REPLACE FUNCTION capture_database_change()
RETURNS TRIGGER AS $$
DECLARE
    v_before_state JSONB;
    v_after_state JSONB;
    v_changed_columns TEXT[];
    v_operation VARCHAR(20);
    v_primary_key VARCHAR(255);
BEGIN
    -- Determine operation type
    IF TG_OP = 'INSERT' THEN
        v_operation := 'INSERT';
        v_after_state := to_jsonb(NEW);
        v_before_state := NULL;
        v_changed_columns := NULL;
        v_primary_key := NEW.id::TEXT; -- Assuming 'id' column, adjust as needed
        
    ELSIF TG_OP = 'UPDATE' THEN
        v_operation := 'UPDATE';
        v_before_state := to_jsonb(OLD);
        v_after_state := to_jsonb(NEW);
        
        -- Determine changed columns
        SELECT ARRAY_AGG(key)
        INTO v_changed_columns
        FROM jsonb_each(to_jsonb(NEW))
        WHERE value IS DISTINCT FROM (to_jsonb(OLD) -> key);
        
        v_primary_key := NEW.id::TEXT;
        
    ELSIF TG_OP = 'DELETE' THEN
        v_operation := 'DELETE';
        v_before_state := to_jsonb(OLD);
        v_after_state := NULL;
        v_changed_columns := NULL;
        v_primary_key := OLD.id::TEXT;
    END IF;
    
    -- Insert into change log
    INSERT INTO database_change_log (
        table_name,
        operation,
        primary_key,
        before_state,
        after_state,
        changed_columns,
        transaction_id,
        changed_at
    ) VALUES (
        TG_TABLE_NAME,
        v_operation,
        v_primary_key,
        v_before_state,
        v_after_state,
        v_changed_columns,
        txid_current()::TEXT,
        NOW()
    );
    
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

-- Create triggers for key tables
-- Example: Playback sessions table
CREATE TRIGGER cdc_playback_sessions_trigger
    AFTER INSERT OR UPDATE OR DELETE ON playback_sessions
    FOR EACH ROW
    EXECUTE FUNCTION capture_database_change();

-- Example: Analytics events table
CREATE TRIGGER cdc_analytics_events_trigger
    AFTER INSERT OR UPDATE OR DELETE ON analytics_events
    FOR EACH ROW
    EXECUTE FUNCTION capture_database_change();

-- Example: ML pipeline runs table
CREATE TRIGGER cdc_ml_pipeline_runs_trigger
    AFTER INSERT OR UPDATE OR DELETE ON ml_pipeline_runs
    FOR EACH ROW
    EXECUTE FUNCTION capture_database_change();

-- Note: Add triggers for other tables as needed
-- The trigger function can be customized per table if needed

COMMENT ON FUNCTION capture_database_change() IS 'Trigger function to capture database changes for CDC';
COMMENT ON TRIGGER cdc_playback_sessions_trigger ON playback_sessions IS 'CDC trigger for playback sessions table';
COMMENT ON TRIGGER cdc_analytics_events_trigger ON analytics_events IS 'CDC trigger for analytics events table';
COMMENT ON TRIGGER cdc_ml_pipeline_runs_trigger ON ml_pipeline_runs IS 'CDC trigger for ML pipeline runs table';


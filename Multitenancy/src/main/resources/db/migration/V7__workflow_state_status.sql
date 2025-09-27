-- Add status mapping to workflow_states to align with Issue.Status

ALTER TABLE workflow_states
    ADD COLUMN IF NOT EXISTS status VARCHAR(20);

-- Optional: backfill typical defaults if empty (no-op by default)
-- UPDATE workflow_states SET status = 'OPEN' WHERE status IS NULL AND is_initial = TRUE;



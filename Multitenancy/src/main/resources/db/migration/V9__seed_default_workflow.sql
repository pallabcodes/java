-- Seed a default workflow for tenant 'acme' and project 'project-1' for demo/testing
-- Safe to run multiple times due to upserts with unique constraints

CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$
DECLARE
    wf_id UUID;
    s_open UUID;
    s_inpr UUID;
    s_review UUID;
    s_resolved UUID;
BEGIN
    -- insert workflow if not exists
    INSERT INTO workflows(id, tenant_id, project_id, key, name, description, is_active)
    VALUES (gen_random_uuid(), 'acme', 'project-1', 'default', 'Default Workflow', 'Default issue workflow', TRUE)
    ON CONFLICT ON CONSTRAINT uk_workflow_tenant_project_key DO NOTHING;

    SELECT id INTO wf_id FROM workflows WHERE tenant_id='acme' AND project_id='project-1' AND key='default' LIMIT 1;

    -- states
    INSERT INTO workflow_states(id, tenant_id, project_id, workflow_id, key, name, is_initial, is_terminal, ordinal, status)
    VALUES (gen_random_uuid(), 'acme', 'project-1', wf_id, 'open', 'Open', TRUE, FALSE, 1, 'OPEN')
    ON CONFLICT ON CONSTRAINT uk_wf_state_tenant_project_wf_key DO NOTHING;

    INSERT INTO workflow_states(id, tenant_id, project_id, workflow_id, key, name, is_initial, is_terminal, ordinal, status)
    VALUES (gen_random_uuid(), 'acme', 'project-1', wf_id, 'in_progress', 'In Progress', FALSE, FALSE, 2, 'IN_PROGRESS')
    ON CONFLICT ON CONSTRAINT uk_wf_state_tenant_project_wf_key DO NOTHING;

    INSERT INTO workflow_states(id, tenant_id, project_id, workflow_id, key, name, is_initial, is_terminal, ordinal, status)
    VALUES (gen_random_uuid(), 'acme', 'project-1', wf_id, 'in_review', 'In Review', FALSE, FALSE, 3, 'IN_REVIEW')
    ON CONFLICT ON CONSTRAINT uk_wf_state_tenant_project_wf_key DO NOTHING;

    INSERT INTO workflow_states(id, tenant_id, project_id, workflow_id, key, name, is_initial, is_terminal, ordinal, status)
    VALUES (gen_random_uuid(), 'acme', 'project-1', wf_id, 'resolved', 'Resolved', FALSE, TRUE, 4, 'RESOLVED')
    ON CONFLICT ON CONSTRAINT uk_wf_state_tenant_project_wf_key DO NOTHING;

    SELECT id INTO s_open FROM workflow_states WHERE tenant_id='acme' AND project_id='project-1' AND workflow_id=wf_id AND key='open' LIMIT 1;
    SELECT id INTO s_inpr FROM workflow_states WHERE tenant_id='acme' AND project_id='project-1' AND workflow_id=wf_id AND key='in_progress' LIMIT 1;
    SELECT id INTO s_review FROM workflow_states WHERE tenant_id='acme' AND project_id='project-1' AND workflow_id=wf_id AND key='in_review' LIMIT 1;
    SELECT id INTO s_resolved FROM workflow_states WHERE tenant_id='acme' AND project_id='project-1' AND workflow_id=wf_id AND key='resolved' LIMIT 1;

    -- transitions
    INSERT INTO workflow_transitions(id, tenant_id, project_id, workflow_id, from_state_id, to_state_id, key, name, required_permission, guard_expression, ordinal)
    VALUES (gen_random_uuid(), 'acme', 'project-1', wf_id, s_open, s_inpr, 'start', 'Start Progress', 'ISSUE_TRANSITION', NULL, 1)
    ON CONFLICT ON CONSTRAINT uk_wf_tr_tenant_project_wf_key DO NOTHING;

    INSERT INTO workflow_transitions(id, tenant_id, project_id, workflow_id, from_state_id, to_state_id, key, name, required_permission, guard_expression, ordinal)
    VALUES (gen_random_uuid(), 'acme', 'project-1', wf_id, s_inpr, s_review, 'submit_review', 'Submit for Review', 'ISSUE_TRANSITION', NULL, 2)
    ON CONFLICT ON CONSTRAINT uk_wf_tr_tenant_project_wf_key DO NOTHING;

    INSERT INTO workflow_transitions(id, tenant_id, project_id, workflow_id, from_state_id, to_state_id, key, name, required_permission, guard_expression, ordinal)
    VALUES (gen_random_uuid(), 'acme', 'project-1', wf_id, s_review, s_resolved, 'resolve', 'Resolve', 'ISSUE_TRANSITION', NULL, 3)
    ON CONFLICT ON CONSTRAINT uk_wf_tr_tenant_project_wf_key DO NOTHING;
END $$;



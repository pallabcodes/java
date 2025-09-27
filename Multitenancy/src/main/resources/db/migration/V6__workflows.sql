-- Workflows schema (tenant and project scoped)
-- Source of truth for DDL is Flyway

CREATE TABLE IF NOT EXISTS workflows (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(50)  NOT NULL,
    project_id      VARCHAR(36)  NOT NULL,
    key             VARCHAR(64)  NOT NULL,
    name            VARCHAR(255) NOT NULL,
    description     VARCHAR(1000),
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- one workflow key per project per tenant
CREATE UNIQUE INDEX IF NOT EXISTS uk_workflow_tenant_project_key
    ON workflows(tenant_id, project_id, key);

CREATE INDEX IF NOT EXISTS idx_workflow_tenant ON workflows(tenant_id);
CREATE INDEX IF NOT EXISTS idx_workflow_project ON workflows(project_id);

CREATE TABLE IF NOT EXISTS workflow_states (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(50)  NOT NULL,
    project_id      VARCHAR(36)  NOT NULL,
    workflow_id     UUID         NOT NULL REFERENCES workflows(id) ON DELETE CASCADE,
    key             VARCHAR(64)  NOT NULL,
    name            VARCHAR(255) NOT NULL,
    is_initial      BOOLEAN      NOT NULL DEFAULT FALSE,
    is_terminal     BOOLEAN      NOT NULL DEFAULT FALSE,
    ordinal         INTEGER      NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- unique state key and ordinal within workflow per tenant/project
CREATE UNIQUE INDEX IF NOT EXISTS uk_wf_state_tenant_project_wf_key
    ON workflow_states(tenant_id, project_id, workflow_id, key);

CREATE UNIQUE INDEX IF NOT EXISTS uk_wf_state_tenant_project_wf_ordinal
    ON workflow_states(tenant_id, project_id, workflow_id, ordinal);

CREATE INDEX IF NOT EXISTS idx_wf_state_tenant ON workflow_states(tenant_id);
CREATE INDEX IF NOT EXISTS idx_wf_state_project ON workflow_states(project_id);
CREATE INDEX IF NOT EXISTS idx_wf_state_wf ON workflow_states(workflow_id);

CREATE TABLE IF NOT EXISTS workflow_transitions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           VARCHAR(50)  NOT NULL,
    project_id          VARCHAR(36)  NOT NULL,
    workflow_id         UUID         NOT NULL REFERENCES workflows(id) ON DELETE CASCADE,
    from_state_id       UUID         NOT NULL REFERENCES workflow_states(id) ON DELETE CASCADE,
    to_state_id         UUID         NOT NULL REFERENCES workflow_states(id) ON DELETE CASCADE,
    key                 VARCHAR(64)  NOT NULL,
    name                VARCHAR(255) NOT NULL,
    required_permission VARCHAR(64),
    guard_expression    VARCHAR(2000), -- placeholder for future policy/SpEL/dsl
    ordinal             INTEGER      NOT NULL,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- unique transition key and ordinal within workflow per tenant/project
CREATE UNIQUE INDEX IF NOT EXISTS uk_wf_tr_tenant_project_wf_key
    ON workflow_transitions(tenant_id, project_id, workflow_id, key);

CREATE UNIQUE INDEX IF NOT EXISTS uk_wf_tr_tenant_project_wf_from_to
    ON workflow_transitions(tenant_id, project_id, workflow_id, from_state_id, to_state_id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_wf_tr_tenant_project_wf_ordinal
    ON workflow_transitions(tenant_id, project_id, workflow_id, ordinal);

CREATE INDEX IF NOT EXISTS idx_wf_tr_tenant ON workflow_transitions(tenant_id);
CREATE INDEX IF NOT EXISTS idx_wf_tr_project ON workflow_transitions(project_id);
CREATE INDEX IF NOT EXISTS idx_wf_tr_wf ON workflow_transitions(workflow_id);

-- helper enum like constraints using check for booleans already
-- ensure only one initial state per workflow
CREATE OR REPLACE FUNCTION enforce_single_initial_state() RETURNS TRIGGER AS $$
BEGIN
    IF NEW.is_initial THEN
        IF EXISTS (
            SELECT 1 FROM workflow_states
            WHERE tenant_id = NEW.tenant_id
              AND project_id = NEW.project_id
              AND workflow_id = NEW.workflow_id
              AND is_initial = TRUE
              AND id <> NEW.id
        ) THEN
            RAISE EXCEPTION 'Only one initial state allowed per workflow';
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_single_initial_state ON workflow_states;
CREATE TRIGGER trg_single_initial_state
    BEFORE INSERT OR UPDATE ON workflow_states
    FOR EACH ROW EXECUTE FUNCTION enforce_single_initial_state();



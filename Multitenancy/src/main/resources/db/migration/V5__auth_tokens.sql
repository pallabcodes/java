-- V5: auth tokens (password reset, refresh tokens)
-- Source of truth for token storage. All tokens are tenant scoped.

-- password reset tokens
create table if not exists password_reset_tokens (
    id bigserial primary key,
    tenant_id varchar(64) not null,
    user_id bigint not null references users(id) on delete cascade,
    token_hash varchar(255) not null,
    expires_at timestamptz not null,
    consumed_at timestamptz,
    created_at timestamptz default now() not null,
    constraint uq_password_reset_token unique (tenant_id, token_hash)
);

-- quick lookups and cleanup
create index if not exists idx_prt_tenant_user on password_reset_tokens(tenant_id, user_id);
create index if not exists idx_prt_expires_at on password_reset_tokens(expires_at);

-- refresh tokens (rotating)
create table if not exists refresh_tokens (
    id bigserial primary key,
    tenant_id varchar(64) not null,
    user_id bigint not null references users(id) on delete cascade,
    token_hash varchar(255) not null,
    parent_token_hash varchar(255), -- for rotation lineage
    user_agent varchar(255),
    ip_address varchar(64),
    expires_at timestamptz not null,
    revoked_at timestamptz,
    created_at timestamptz default now() not null,
    constraint uq_refresh_token unique (tenant_id, token_hash)
);

create index if not exists idx_rt_tenant_user on refresh_tokens(tenant_id, user_id);
create index if not exists idx_rt_expires_at on refresh_tokens(expires_at);
create index if not exists idx_rt_parent on refresh_tokens(parent_token_hash);

-- housekeeping: partial index for active tokens only
create index if not exists idx_rt_active on refresh_tokens(tenant_id, user_id)
where revoked_at is null and expires_at > now();



-- Initial schema for projects and issues (multi-tenant)

create table if not exists projects (
    id                  varchar(36)  not null primary key,
    tenant_id           varchar(50)  not null,
    key                 varchar(10)  not null,
    name                varchar(255) not null,
    description         varchar(1000),
    status              varchar(20)  not null,
    type                varchar(20)  not null,
    owner_id            varchar(36)  not null,
    created_at          timestamp    not null default now(),
    updated_at          timestamp    not null default now(),
    version             bigint       not null default 0,
    deleted_at          timestamp,
    constraint uk_project_tenant_key unique (tenant_id, key)
);

create index if not exists idx_project_tenant_id on projects(tenant_id);
create index if not exists idx_project_key on projects(key);
create index if not exists idx_project_owner_id on projects(owner_id);
create index if not exists idx_project_status on projects(status);
create index if not exists idx_project_type on projects(type);
create index if not exists idx_project_created_at on projects(created_at);
create index if not exists idx_project_updated_at on projects(updated_at);
create index if not exists idx_project_deleted_at on projects(deleted_at);

create table if not exists issues (
    id                  varchar(36)  not null primary key,
    tenant_id           varchar(50)  not null,
    key                 varchar(20)  not null,
    title               varchar(255) not null,
    description         varchar(5000),
    status              varchar(20)  not null,
    priority            varchar(20)  not null,
    type                varchar(20)  not null,
    project_id          varchar(36)  not null,
    assignee_id         varchar(36),
    reporter_id         varchar(36)  not null,
    story_points        integer,
    time_estimate       bigint,
    time_spent          bigint,
    due_date            timestamp,
    created_at          timestamp    not null default now(),
    updated_at          timestamp    not null default now(),
    version             bigint       not null default 0,
    deleted_at          timestamp,
    constraint uk_issue_tenant_key unique (tenant_id, key)
);

create index if not exists idx_issue_tenant_id on issues(tenant_id);
create index if not exists idx_issue_project_id on issues(project_id);
create index if not exists idx_issue_assignee_id on issues(assignee_id);
create index if not exists idx_issue_reporter_id on issues(reporter_id);
create index if not exists idx_issue_status on issues(status);
create index if not exists idx_issue_priority on issues(priority);
create index if not exists idx_issue_type on issues(type);
create index if not exists idx_issue_created_at on issues(created_at);
create index if not exists idx_issue_updated_at on issues(updated_at);
create index if not exists idx_issue_deleted_at on issues(deleted_at);



create table if not exists users (
    id varchar(36) primary key,
    tenant_id varchar(50) not null,
    email varchar(255) not null,
    username varchar(60) not null,
    password_hash varchar(255) not null,
    enabled boolean not null default true,
    locked boolean not null default false,
    token_version int not null default 0,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now(),
    constraint uk_users_tenant_email unique (tenant_id, email),
    constraint uk_users_tenant_username unique (tenant_id, username)
);

create index if not exists idx_users_tenant_id on users(tenant_id);
create index if not exists idx_users_email on users(email);
create index if not exists idx_users_username on users(username);

create table if not exists roles (
    id bigserial primary key,
    tenant_id varchar(50) not null,
    name varchar(50) not null,
    constraint uk_roles_tenant_name unique (tenant_id, name)
);

create table if not exists user_roles (
    id bigserial primary key,
    tenant_id varchar(50) not null,
    user_id varchar(36) not null,
    role_id bigint not null,
    constraint uk_user_roles unique (tenant_id, user_id, role_id)
);


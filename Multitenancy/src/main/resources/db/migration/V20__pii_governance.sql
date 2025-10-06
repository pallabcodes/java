-- PII classification and retention
create table if not exists pii_catalog (
  id           varchar(36) primary key,
  entity_name  varchar(100) not null,
  field_name   varchar(100) not null,
  classification varchar(50) not null, -- public, internal, confidential, restricted
  retention_days integer not null default 365,
  created_at   timestamp not null default current_timestamp
);
create unique index if not exists uk_pii_catalog on pii_catalog(entity_name, field_name);

create table if not exists deletion_requests (
  id           varchar(36) primary key,
  tenant_id    varchar(36) not null,
  subject_id   varchar(100) not null,
  status       varchar(20) not null default 'PENDING',
  created_at   timestamp not null default current_timestamp,
  processed_at timestamp
);
create index if not exists idx_deletion_requests_status on deletion_requests(status);


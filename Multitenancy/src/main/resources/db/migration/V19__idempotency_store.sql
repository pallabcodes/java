-- Idempotency store for exactly-once consumer processing
create table if not exists idempotency_keys (
    tenant_id      varchar(36) not null,
    consumer_name  varchar(100) not null,
    message_key    varchar(256) not null,
    processed_at   timestamp not null default current_timestamp,
    primary key (tenant_id, consumer_name, message_key)
);

create index if not exists idx_idem_processed_at on idempotency_keys(processed_at);


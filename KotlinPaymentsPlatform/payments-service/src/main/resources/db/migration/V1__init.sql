create table payment_intent (
  id varchar(64) primary key,
  amount_minor bigint not null,
  currency varchar(3) not null,
  status varchar(16) not null,
  idempotency_key varchar(128) not null,
  created_at timestamp not null default current_timestamp,
  updated_at timestamp not null default current_timestamp
);
create unique index uq_intent_idem on payment_intent(idempotency_key);

create table webhook_outbox (
  id bigserial primary key,
  aggregate_id varchar(64) not null,
  event_type varchar(64) not null,
  payload json not null,
  created_at timestamp not null default current_timestamp,
  delivered boolean not null default false
);
create index idx_outbox_delivered on webhook_outbox(delivered, created_at);


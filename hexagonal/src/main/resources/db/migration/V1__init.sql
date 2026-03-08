create table samples (
  id uuid primary key,
  name varchar(255) not null,
  status varchar(32) not null,
  created_at timestamptz not null
);

create table outbox_events (
  id uuid primary key,
  aggregate_id uuid not null,
  aggregate_type varchar(128) not null,
  event_type varchar(128) not null,
  payload jsonb not null,
  occurred_at timestamptz not null,
  processed_at timestamptz
);

create table account (
  id varchar(64) primary key,
  currency varchar(3) not null,
  created_at timestamp not null default current_timestamp
);

create table journal (
  id varchar(64) primary key,
  created_at timestamp not null default current_timestamp
);

create table ledger_entry (
  id varchar(64) primary key,
  journal_id varchar(64) not null references journal(id),
  account_id varchar(64) not null references account(id),
  amount_minor bigint not null,
  currency varchar(3) not null,
  direction varchar(6) not null,
  description varchar(256) not null,
  created_at timestamp not null default current_timestamp
);

create index idx_ledger_entry_journal on ledger_entry(journal_id);
create index idx_ledger_entry_account on ledger_entry(account_id);


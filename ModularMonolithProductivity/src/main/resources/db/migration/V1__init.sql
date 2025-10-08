create table if not exists projects (
  id varchar(64) primary key,
  project_key varchar(16) not null,
  name varchar(128) not null,
  description varchar(1024)
);
create unique index if not exists idx_projects_key on projects(project_key);
create unique index if not exists idx_projects_name on projects(name);

create table if not exists issues (
  id varchar(64) primary key,
  project_id varchar(64) not null,
  title varchar(256) not null,
  description varchar(2048),
  status varchar(32) not null
);
create index if not exists idx_issues_project on issues(project_id);


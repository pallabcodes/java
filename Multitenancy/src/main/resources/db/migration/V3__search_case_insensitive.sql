-- Case-insensitive trigram search support
create index if not exists idx_issues_lower_title_trgm on issues using gin (lower(title) gin_trgm_ops);
create index if not exists idx_issues_lower_description_trgm on issues using gin (lower(description) gin_trgm_ops);


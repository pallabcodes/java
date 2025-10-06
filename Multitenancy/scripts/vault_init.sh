#!/usr/bin/env bash

set -euo pipefail

VAULT_ADDR_ENV=${VAULT_ADDR:-http://localhost:8200}
VAULT_TOKEN_ENV=${VAULT_TOKEN:-root}

export VAULT_ADDR="$VAULT_ADDR_ENV"
export VAULT_TOKEN="$VAULT_TOKEN_ENV"

echo "enabling database secrets engine"
vault secrets enable -path=database database >/dev/null 2>&1 || true

echo "configuring reporting db"
vault write database/config/reporting_db \
  plugin_name=postgresql-database-plugin \
  allowed_roles=reporting_role \
  connection_url="postgresql://{{username}}:{{password}}@reporting-db:5432/reporting_db?sslmode=disable" \
  username="reporting_user" \
  password="reporting_pass"

echo "configuring attachments db"
vault write database/config/attachments_db \
  plugin_name=postgresql-database-plugin \
  allowed_roles=attachments_role \
  connection_url="postgresql://{{username}}:{{password}}@attachments-db:5432/attachments_db?sslmode=disable" \
  username="attachments_user" \
  password="attachments_pass"

echo "creating reporting role"
vault write database/roles/reporting_role \
  db_name=reporting_db \
  creation_statements="CREATE ROLE \"{{name}}\" WITH LOGIN PASSWORD '{{password}}' VALID UNTIL '{{expiration}}'; GRANT CONNECT ON DATABASE reporting_db TO \"{{name}}\"; GRANT USAGE ON SCHEMA public TO \"{{name}}\"; GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO \"{{name}}\"; ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO \"{{name}}\";" \
  default_ttl=1h \
  max_ttl=24h

echo "creating attachments role"
vault write database/roles/attachments_role \
  db_name=attachments_db \
  creation_statements="CREATE ROLE \"{{name}}\" WITH LOGIN PASSWORD '{{password}}' VALID UNTIL '{{expiration}}'; GRANT CONNECT ON DATABASE attachments_db TO \"{{name}}\"; GRANT USAGE ON SCHEMA public TO \"{{name}}\"; GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO \"{{name}}\"; ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO \"{{name}}\";" \
  default_ttl=1h \
  max_ttl=24h

echo "done"



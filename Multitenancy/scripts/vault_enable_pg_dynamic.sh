#!/usr/bin/env bash
set -euo pipefail

VAULT_ADDR=${VAULT_ADDR:-http://localhost:8200}
VAULT_TOKEN=${VAULT_TOKEN:-root}
PG_HOST=${PG_HOST:-localhost}
PG_PORT=${PG_PORT:-5432}
PG_DB=${PG_DB:-productivity}
PG_ADMIN_USER=${PG_ADMIN_USER:-postgres}
PG_ADMIN_PASS=${PG_ADMIN_PASS:-postgres}

export VAULT_ADDR
export VAULT_TOKEN

echo "Enabling database secrets engine"
vault secrets enable -path=db database || true

echo "Configuring Postgres plugin"
vault write db/config/productivity \
  plugin_name=postgresql-database-plugin \
  allowed_roles="productivity-app" \
  connection_url="postgresql://{{username}}:{{password}}@${PG_HOST}:${PG_PORT}/${PG_DB}?sslmode=disable" \
  username="${PG_ADMIN_USER}" \
  password="${PG_ADMIN_PASS}"

echo "Creating app role with rotation"
vault write db/roles/productivity-app \
  db_name=productivity \
  creation_statements="CREATE ROLE \"{{name}}\" WITH LOGIN PASSWORD '\"{{password}}\"' VALID UNTIL '{{expiration}}'; GRANT CONNECT ON DATABASE ${PG_DB} TO \"{{name}}\";" \
  default_ttl=1h \
  max_ttl=24h

echo "Done. Read creds via: vault read db/creds/productivity-app"


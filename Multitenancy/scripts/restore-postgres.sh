#!/usr/bin/env bash
set -euo pipefail

DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-5432}
DB_NAME=${DB_NAME:-productivity_platform}
DB_USER=${DB_USER:-productivity_user}
BACKUP_FILE=${1:-backup.sql}

psql "host=$DB_HOST port=$DB_PORT dbname=$DB_NAME user=$DB_USER" -f "$BACKUP_FILE"
echo "restore complete"


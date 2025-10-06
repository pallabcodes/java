#!/usr/bin/env bash
set -euo pipefail

PG_HOST=${PG_HOST:-localhost}
PG_PORT=${PG_PORT:-5432}
PG_DB=${PG_DB:-productivity_test}
PG_USER=${PG_USER:-postgres}
PG_PASSWORD=${PG_PASSWORD:-postgres}
RTO_THRESHOLD_SEC=${RTO_THRESHOLD_SEC:-60}

export PGPASSWORD="$PG_PASSWORD"

echo "Creating sample table"
psql -h "$PG_HOST" -p "$PG_PORT" -U "$PG_USER" -d "$PG_DB" -c "CREATE TABLE IF NOT EXISTS rto_probe(id serial primary key, ts timestamptz default now()); INSERT INTO rto_probe DEFAULT VALUES;"

START=$(date +%s)
echo "Backing up database"
pg_dump -h "$PG_HOST" -p "$PG_PORT" -U "$PG_USER" -d "$PG_DB" > backup.sql

echo "Dropping table"
psql -h "$PG_HOST" -p "$PG_PORT" -U "$PG_USER" -d "$PG_DB" -c "DROP TABLE rto_probe;"

echo "Restoring database"
psql -h "$PG_HOST" -p "$PG_PORT" -U "$PG_USER" -d "$PG_DB" < backup.sql

END=$(date +%s)
ELAPSED=$((END-START))
echo "Backup+restore elapsed: ${ELAPSED}s"

if [ "$ELAPSED" -gt "$RTO_THRESHOLD_SEC" ]; then
  echo "RTO exceeded threshold ${RTO_THRESHOLD_SEC}s"
  exit 1
fi

echo "RTO within threshold"


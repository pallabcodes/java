#!/usr/bin/env bash
set -euo pipefail

SCHEMA_REGISTRY_URL=${SCHEMA_REGISTRY_URL:-http://localhost:8085}
SUBJECT=${1:?subject required}
SCHEMA_FILE=${2:?schema file required}

curl -sf -X POST \
  -H 'Content-Type: application/vnd.schemaregistry.v1+json' \
  --data "{\"schema\":$(jq -c . "$SCHEMA_FILE")}" \
  "$SCHEMA_REGISTRY_URL/subjects/$SUBJECT/versions"
echo "registered $SUBJECT from $SCHEMA_FILE"


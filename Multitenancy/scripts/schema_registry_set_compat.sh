#!/usr/bin/env bash
set -euo pipefail

SCHEMA_REGISTRY_URL=${SCHEMA_REGISTRY_URL:-http://localhost:8085}
SUBJECT=${1:?subject required}
LEVEL=${2:-BACKWARD}

curl -sf -X PUT \
  -H 'Content-Type: application/vnd.schemaregistry.v1+json' \
  --data "{\"compatibility\":\"$LEVEL\"}" \
  "$SCHEMA_REGISTRY_URL/config/$SUBJECT"
echo "compatibility for $SUBJECT set to $LEVEL"


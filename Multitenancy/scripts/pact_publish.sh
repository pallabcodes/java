#!/usr/bin/env bash
set -euo pipefail

PACT_BROKER_BASE_URL=${PACT_BROKER_BASE_URL:-http://localhost:9292}
VERSION=${VERSION:-$(git rev-parse --short HEAD)}

for pact in pacts/*.json; do
  [ -e "$pact" ] || continue
  curl -sf -X PUT \
    -H "Content-Type: application/json" \
    --data-binary @"$pact" \
    "$PACT_BROKER_BASE_URL/pacts/provider/Multitenancy/consumer/$(basename "$pact" .json)/version/$VERSION"
done
echo "pacts published"


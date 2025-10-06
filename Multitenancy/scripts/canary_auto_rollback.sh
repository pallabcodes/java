#!/usr/bin/env bash
set -euo pipefail

APP_NS=${APP_NS:-productivity}
VS_NAME=${VS_NAME:-core}
MAX_BURN_RATE=${MAX_BURN_RATE:-14}

# Requires Prometheus reachable and a query for 5xx error ratio similar to alerts
PROM_URL=${PROM_URL:-http://localhost:9090}
QUERY='(sum(rate(http_requests_total{status=~"5.."}[5m])) / sum(rate(http_requests_total[5m]))) / (1 - 0.99)'

burn=$(curl -s --get --data-urlencode "query=${QUERY}" "$PROM_URL/api/v1/query" | jq -r '.data.result[0].value[1]' || echo 0)
echo "Current burn: $burn"

bc_comp=$(awk -v a="$burn" -v b="$MAX_BURN_RATE" 'BEGIN {print (a>b)?1:0}')
if [ "$bc_comp" -eq 1 ]; then
  echo "Burn above threshold, rolling back canary"
  kubectl -n "$APP_NS" rollout undo deployment "$VS_NAME"
else
  echo "Burn within limits"
fi


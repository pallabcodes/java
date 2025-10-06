#!/usr/bin/env bash
set -euo pipefail

APP_HOST=${APP_HOST:-http://localhost}
PATHS=(/actuator/health /api/health/ping)
THRESHOLD=${THRESHOLD:-3}

fail_count=0
for p in "${PATHS[@]}"; do
  if ! curl -sf "${APP_HOST}${p}" > /dev/null; then
    fail_count=$((fail_count+1))
  fi
done

if [ "$fail_count" -ge "$THRESHOLD" ]; then
  echo "Failover conditions met (${fail_count} failures)."
  exit 0
else
  echo "Failover not triggered (${fail_count} failures)."
  exit 1
fi


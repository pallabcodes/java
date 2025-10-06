#!/usr/bin/env bash
set -euo pipefail

TARGET_URL=${TARGET_URL:-http://localhost:8080}
DURATION=${DURATION:-10m}
THRESHOLD_ERR_RATE=${THRESHOLD_ERR_RATE:-0.02}

cat > k6-soak.js <<'EOF'
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 20,
  duration: `${__ENV.DURATION}`,
  thresholds: {
    http_req_failed: [`rate<${__ENV.THRESHOLD_ERR_RATE}`],
    http_req_duration: ['p(95)<1500']
  }
};

export default function () {
  const res = http.get(`${__ENV.TARGET_URL}/actuator/health`);
  check(res, { 'status is 200': (r) => r.status === 200 });
  sleep(1);
}
EOF

docker run --rm -i -e TARGET_URL="$TARGET_URL" -e DURATION="$DURATION" -e THRESHOLD_ERR_RATE="$THRESHOLD_ERR_RATE" grafana/k6 run - < k6-soak.js


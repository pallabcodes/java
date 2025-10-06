#!/usr/bin/env bash
set -euo pipefail

TARGET_URL=${TARGET_URL:-http://localhost:8080}
MAX_P95_MS=${MAX_P95_MS:-1500}

start=$(date +%s)
durations=()
for i in {1..50}; do
  t0=$(date +%s%3N)
  status=$(curl -s -o /dev/null -w "%{http_code}" "$TARGET_URL/actuator/health") || true
  t1=$(date +%s%3N)
  durations+=( $((t1 - t0)) )
  if [ "$status" != "200" ]; then
    echo "Non-200 detected during steady check"
    exit 1
  fi
  sleep 0.2
done

sorted=$(printf "%s\n" "${durations[@]}" | sort -n)
count=${#durations[@]}
index=$(( (95 * count + 99) / 100 ))
p95=$(printf "%s\n" $sorted | sed -n "${index}p")

echo "p95=${p95}ms"
if [ "$p95" -gt "$MAX_P95_MS" ]; then
  echo "p95 above threshold"
  exit 1
fi
echo "Steady state OK"


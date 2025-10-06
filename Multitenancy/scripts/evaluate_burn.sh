#!/usr/bin/env bash
set -euo pipefail

SLO_BURN_THRESHOLD=${SLO_BURN_THRESHOLD:-2.0}
CURRENT_BURN=${CURRENT_BURN:-1.0}

echo "burn=${CURRENT_BURN} threshold=${SLO_BURN_THRESHOLD}"
awk -v burn="$CURRENT_BURN" -v thr="$SLO_BURN_THRESHOLD" 'BEGIN { exit !(burn > thr) }' || exit 0
echo "burn above threshold, rollback advised"
exit 42


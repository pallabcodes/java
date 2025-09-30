#!/bin/bash

set -euo pipefail

if [ $# -ne 2 ]; then
  echo "Usage: $0 <stable_weight> <canary_weight>"
  echo "Example: $0 90 10"
  exit 1
fi

STABLE_WEIGHT=$1
CANARY_WEIGHT=$2

VS_FILE="k8s/istio/virtualservice-gateway.yaml"

if ! command -v yq >/dev/null 2>&1; then
  echo "yq is required. Install with: brew install yq"
  exit 1
fi

TMP=$(mktemp)

# Update weights in VirtualService
cat "$VS_FILE" | yq \
  'select(.kind=="VirtualService").spec.http[0].route[0].weight='

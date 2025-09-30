#!/bin/bash

# Usage: istio-canary-set.sh <namespace> <vs-name> <v1-weight> <v2-weight>
set -euo pipefail

NS=${1:-productivity}
VS=${2:-productivity-api}
W1=${3:-90}
W2=${4:-10}

FILE="k8s/istio/virtualservice-gateway.yaml"

# Simple sed based switch for two routes (v1 then v2) weights
# This assumes the canonical order in the repo

sed -i.bak -E "0,/(weight: )[0-9]+/ s//\1${W1}/" "$FILE"
sed -i.bak -E "0,/(subset: v2)([\s\S]*?weight: )[0-9]+/ s//\1\2${W2}/" "$FILE"

kubectl apply -f "$FILE" -n "$NS"

echo "Applied canary weights v1=${W1} v2=${W2} in namespace ${NS}"

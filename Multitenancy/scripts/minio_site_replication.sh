#!/usr/bin/env bash
set -euo pipefail

# Requires mc (MinIO client) configured for two sites: site1, site2
BUCKET=${BUCKET:-attachments}

mc mb -p site1/${BUCKET} || true
mc mb -p site2/${BUCKET} || true

mc admin bucket remote add site1/${BUCKET} https://site2 MINIO_SITE2 --service replication --sync
mc replicate add site1/${BUCKET} --remote-bucket ${BUCKET} --remote-service arn:minio:replication::MINIO_SITE2:site2 --priority 1 --storage-class STANDARD

echo "Replication configured for bucket ${BUCKET}"


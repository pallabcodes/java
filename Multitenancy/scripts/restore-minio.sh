#!/usr/bin/env bash
set -euo pipefail

MC_ALIAS=${MC_ALIAS:-local}
MINIO_ENDPOINT=${MINIO_ENDPOINT:-http://localhost:9000}
MINIO_ACCESS_KEY=${MINIO_ACCESS_KEY:-minioadmin}
MINIO_SECRET_KEY=${MINIO_SECRET_KEY:-minioadmin}
BUCKET=${BUCKET:-productivity-attachments}
BACKUP_DIR=${1:-./minio-backup}

mc alias set "$MC_ALIAS" "$MINIO_ENDPOINT" "$MINIO_ACCESS_KEY" "$MINIO_SECRET_KEY"
mc mb -p "$MC_ALIAS/$BUCKET" || true
mc mirror "$BACKUP_DIR" "$MC_ALIAS/$BUCKET"
echo "restore complete"


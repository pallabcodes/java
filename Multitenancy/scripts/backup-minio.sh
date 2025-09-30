#!/bin/bash

# MinIO backup script for productivity platform
# Usage: ./backup-minio.sh [bucket_name] [backup_dir]

set -e

BUCKET_NAME=${1:-productivity-attachments}
BACKUP_DIR=${2:-./backups}
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="${BACKUP_DIR}/minio_${BUCKET_NAME}_${TIMESTAMP}.tar.gz"

# MinIO configuration
MINIO_ENDPOINT=${MINIO_ENDPOINT:-http://localhost:9000}
MINIO_ACCESS_KEY=${MINIO_ACCESS_KEY:-minioadmin}
MINIO_SECRET_KEY=${MINIO_SECRET_KEY:-minioadmin}

# Create backup directory if it doesn't exist
mkdir -p "$BACKUP_DIR"

echo "Starting MinIO backup for bucket: $BUCKET_NAME"
echo "Backup file: $BACKUP_FILE"

# Check if mc (MinIO client) is installed
if ! command -v mc &> /dev/null; then
    echo "MinIO client (mc) not found. Installing..."
    curl -O https://dl.min.io/client/mc/release/linux-amd64/mc
    chmod +x mc
    sudo mv mc /usr/local/bin/
fi

# Configure MinIO client
mc alias set local "$MINIO_ENDPOINT" "$MINIO_ACCESS_KEY" "$MINIO_SECRET_KEY"

# Create temporary directory for backup
TEMP_DIR="/tmp/minio_backup_${TIMESTAMP}"
mkdir -p "$TEMP_DIR"

# Download bucket contents
mc mirror "local/$BUCKET_NAME" "$TEMP_DIR"

# Create compressed archive
tar -czf "$BACKUP_FILE" -C "$TEMP_DIR" .

# Cleanup temporary directory
rm -rf "$TEMP_DIR"

echo "Backup completed: $BACKUP_FILE"

# Keep only last 7 days of backups
find "$BACKUP_DIR" -name "minio_${BUCKET_NAME}_*.tar.gz" -mtime +7 -delete
echo "Cleaned up backups older than 7 days"

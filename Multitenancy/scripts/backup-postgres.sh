#!/bin/bash

# PostgreSQL backup script for productivity platform
# Usage: ./backup-postgres.sh [database_name] [backup_dir]

set -e

DB_NAME=${1:-productivity}
BACKUP_DIR=${2:-./backups}
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="${BACKUP_DIR}/postgres_${DB_NAME}_${TIMESTAMP}.sql"

# Create backup directory if it doesn't exist
mkdir -p "$BACKUP_DIR"

echo "Starting PostgreSQL backup for database: $DB_NAME"
echo "Backup file: $BACKUP_FILE"

# Perform the backup
pg_dump -h localhost -U productivity_user -d "$DB_NAME" \
    --verbose \
    --clean \
    --create \
    --if-exists \
    --format=plain \
    --file="$BACKUP_FILE"

# Compress the backup
gzip "$BACKUP_FILE"
echo "Backup completed and compressed: ${BACKUP_FILE}.gz"

# Keep only last 7 days of backups
find "$BACKUP_DIR" -name "postgres_${DB_NAME}_*.sql.gz" -mtime +7 -delete
echo "Cleaned up backups older than 7 days"

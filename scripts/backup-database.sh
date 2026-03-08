#!/bin/bash

# Script de backup automático do PostgreSQL
# Este script cria backups diários do banco de dados do sistema

# Configurações
BACKUP_DIR="/var/backups/sindicato-rural"
DB_NAME="${POSTGRES_DB:-sindicato_rural}"
DB_USER="${POSTGRES_USER:-postgres}"
DB_HOST="${POSTGRES_HOST:-localhost}"
DB_PORT="${POSTGRES_PORT:-5432}"
RETENTION_DAYS=30

# Criar diretório de backup se não existir
mkdir -p "$BACKUP_DIR"

# Nome do arquivo de backup com timestamp
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="$BACKUP_DIR/backup_${DB_NAME}_${TIMESTAMP}.sql.gz"

# Log
echo "[$(date)] Iniciando backup do banco de dados $DB_NAME..."

# Executar backup usando pg_dump
PGPASSWORD="${POSTGRES_PASSWORD}" pg_dump \
    -h "$DB_HOST" \
    -p "$DB_PORT" \
    -U "$DB_USER" \
    -d "$DB_NAME" \
    --format=plain \
    --no-owner \
    --no-acl \
    --verbose \
    2>&1 | gzip > "$BACKUP_FILE"

# Verificar se o backup foi bem-sucedido
if [ $? -eq 0 ]; then
    echo "[$(date)] Backup criado com sucesso: $BACKUP_FILE"
    
    # Calcular tamanho do backup
    BACKUP_SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
    echo "[$(date)] Tamanho do backup: $BACKUP_SIZE"
    
    # Remover backups antigos (mais de RETENTION_DAYS dias)
    echo "[$(date)] Removendo backups com mais de $RETENTION_DAYS dias..."
    find "$BACKUP_DIR" -name "backup_${DB_NAME}_*.sql.gz" -type f -mtime +$RETENTION_DAYS -delete
    
    # Listar backups existentes
    echo "[$(date)] Backups existentes:"
    ls -lh "$BACKUP_DIR"/backup_${DB_NAME}_*.sql.gz
    
    echo "[$(date)] Backup concluído com sucesso!"
    exit 0
else
    echo "[$(date)] ERRO: Falha ao criar backup!"
    exit 1
fi

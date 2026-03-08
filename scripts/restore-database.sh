#!/bin/bash

# Script de restauração do backup do PostgreSQL
# Este script restaura um backup específico do banco de dados

# Configurações
BACKUP_DIR="/var/backups/sindicato-rural"
DB_NAME="${POSTGRES_DB:-sindicato_rural}"
DB_USER="${POSTGRES_USER:-postgres}"
DB_HOST="${POSTGRES_HOST:-localhost}"
DB_PORT="${POSTGRES_PORT:-5432}"

# Verificar se foi fornecido um arquivo de backup
if [ -z "$1" ]; then
    echo "Uso: $0 <arquivo_backup.sql.gz>"
    echo ""
    echo "Backups disponíveis:"
    ls -lh "$BACKUP_DIR"/backup_${DB_NAME}_*.sql.gz
    exit 1
fi

BACKUP_FILE="$1"

# Verificar se o arquivo existe
if [ ! -f "$BACKUP_FILE" ]; then
    echo "ERRO: Arquivo de backup não encontrado: $BACKUP_FILE"
    exit 1
fi

# Confirmação
echo "ATENÇÃO: Esta operação irá SUBSTITUIR todos os dados do banco $DB_NAME!"
echo "Arquivo de backup: $BACKUP_FILE"
read -p "Deseja continuar? (sim/não): " CONFIRM

if [ "$CONFIRM" != "sim" ]; then
    echo "Operação cancelada."
    exit 0
fi

# Log
echo "[$(date)] Iniciando restauração do banco de dados $DB_NAME..."

# Criar backup de segurança antes da restauração
SAFETY_BACKUP="$BACKUP_DIR/pre_restore_backup_$(date +%Y%m%d_%H%M%S).sql.gz"
echo "[$(date)] Criando backup de segurança: $SAFETY_BACKUP"
PGPASSWORD="${POSTGRES_PASSWORD}" pg_dump \
    -h "$DB_HOST" \
    -p "$DB_PORT" \
    -U "$DB_USER" \
    -d "$DB_NAME" \
    --format=plain \
    --no-owner \
    --no-acl \
    2>&1 | gzip > "$SAFETY_BACKUP"

# Desconectar todas as conexões ativas
echo "[$(date)] Desconectando conexões ativas..."
PGPASSWORD="${POSTGRES_PASSWORD}" psql \
    -h "$DB_HOST" \
    -p "$DB_PORT" \
    -U "$DB_USER" \
    -d postgres \
    -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '$DB_NAME' AND pid <> pg_backend_pid();"

# Dropar e recriar o banco de dados
echo "[$(date)] Recriando banco de dados..."
PGPASSWORD="${POSTGRES_PASSWORD}" psql \
    -h "$DB_HOST" \
    -p "$DB_PORT" \
    -U "$DB_USER" \
    -d postgres \
    -c "DROP DATABASE IF EXISTS $DB_NAME;"

PGPASSWORD="${POSTGRES_PASSWORD}" psql \
    -h "$DB_HOST" \
    -p "$DB_PORT" \
    -U "$DB_USER" \
    -d postgres \
    -c "CREATE DATABASE $DB_NAME;"

# Restaurar o backup
echo "[$(date)] Restaurando backup..."
gunzip -c "$BACKUP_FILE" | PGPASSWORD="${POSTGRES_PASSWORD}" psql \
    -h "$DB_HOST" \
    -p "$DB_PORT" \
    -U "$DB_USER" \
    -d "$DB_NAME" \
    --quiet

# Verificar se a restauração foi bem-sucedida
if [ $? -eq 0 ]; then
    echo "[$(date)] Restauração concluída com sucesso!"
    echo "[$(date)] Backup de segurança mantido em: $SAFETY_BACKUP"
    exit 0
else
    echo "[$(date)] ERRO: Falha ao restaurar backup!"
    echo "[$(date)] Backup de segurança disponível em: $SAFETY_BACKUP"
    exit 1
fi

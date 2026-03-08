#!/bin/bash

# Script para configurar backup automático diário via cron
# Executa o backup todos os dias às 2:00 AM

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKUP_SCRIPT="$SCRIPT_DIR/backup-database.sh"
LOG_FILE="/var/log/sindicato-rural-backup.log"

# Verificar se o script de backup existe
if [ ! -f "$BACKUP_SCRIPT" ]; then
    echo "ERRO: Script de backup não encontrado: $BACKUP_SCRIPT"
    exit 1
fi

# Tornar o script executável
chmod +x "$BACKUP_SCRIPT"

# Criar entrada no crontab
CRON_ENTRY="0 2 * * * $BACKUP_SCRIPT >> $LOG_FILE 2>&1"

# Verificar se a entrada já existe
if crontab -l 2>/dev/null | grep -q "$BACKUP_SCRIPT"; then
    echo "Backup automático já está configurado no crontab."
    echo "Entrada atual:"
    crontab -l | grep "$BACKUP_SCRIPT"
else
    # Adicionar entrada ao crontab
    (crontab -l 2>/dev/null; echo "$CRON_ENTRY") | crontab -
    echo "Backup automático configurado com sucesso!"
    echo "O backup será executado diariamente às 2:00 AM"
    echo "Logs serão salvos em: $LOG_FILE"
fi

# Exibir crontab atual
echo ""
echo "Crontab atual:"
crontab -l

echo ""
echo "Para remover o backup automático, execute:"
echo "crontab -e"
echo "E remova a linha contendo: $BACKUP_SCRIPT"

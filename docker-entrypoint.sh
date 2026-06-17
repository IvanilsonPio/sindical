#!/bin/sh
set -e

UPLOAD_DIR="${FILE_UPLOAD_DIR:-/data/uploads}"

# Aguarda até 30s pelo volume ser montado pelo Railway
# O Railway monta o volume depois que o container inicia
echo "Aguardando montagem do volume em ${UPLOAD_DIR}..."
i=0
while [ $i -lt 30 ]; do
  if mountpoint -q "${UPLOAD_DIR}" 2>/dev/null || [ -d "${UPLOAD_DIR}" ]; then
    echo "Volume disponível em ${UPLOAD_DIR}"
    break
  fi
  sleep 1
  i=$((i + 1))
done

# Cria subdiretórios e ajusta permissões
mkdir -p "${UPLOAD_DIR}/arquivos-gerais" "${UPLOAD_DIR}/recibos" /app/logs
chown -R spring:spring "${UPLOAD_DIR}" /app/logs 2>/dev/null || true

exec su-exec spring java \
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=60.0 \
  -XX:InitialRAMPercentage=30.0 \
  -XX:+UseSerialGC \
  -XX:MaxMetaspaceSize=128m \
  -Djava.security.egd=file:/dev/./urandom \
  -Dspring.jmx.enabled=false \
  -Dspring.profiles.active=prod \
  -jar app.jar "$@"

#!/bin/sh
set -e

UPLOAD_DIR="${FILE_UPLOAD_DIR:-/data/uploads}"

echo "=== PORT=${PORT} FILE_UPLOAD_DIR=${UPLOAD_DIR} ==="

# Cria subdiretórios no volume (já montado antes do container iniciar)
mkdir -p "${UPLOAD_DIR}/arquivos-gerais" "${UPLOAD_DIR}/recibos" /app/logs

exec java \
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=60.0 \
  -XX:InitialRAMPercentage=30.0 \
  -XX:+UseSerialGC \
  -XX:MaxMetaspaceSize=128m \
  -Djava.security.egd=file:/dev/./urandom \
  -Dspring.jmx.enabled=false \
  -Dspring.profiles.active=prod \
  -Dserver.port="${PORT:-8080}" \
  -jar app.jar "$@"

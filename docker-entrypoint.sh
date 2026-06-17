#!/bin/sh
set -e

UPLOAD_DIR="${FILE_UPLOAD_DIR:-/data/uploads}"

# Cria diretórios e corrige permissões (roda como root antes do drop)
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

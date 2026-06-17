#!/bin/sh
set -e

UPLOAD_DIR="${FILE_UPLOAD_DIR:-/data/uploads}"

# Aguarda o volume ser montado pelo Railway
# Detecta montagem real verificando /proc/mounts, não apenas se o diretório existe
echo "Aguardando montagem do volume em ${UPLOAD_DIR}..."
i=0
while [ $i -lt 60 ]; do
  if grep -q "${UPLOAD_DIR}" /proc/mounts 2>/dev/null; then
    echo "Volume montado em ${UPLOAD_DIR}"
    break
  fi
  sleep 1
  i=$((i + 1))
done

if [ $i -eq 60 ]; then
  echo "Timeout aguardando volume. Continuando com diretório local..."
fi

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

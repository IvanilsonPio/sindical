#!/bin/sh
set -e

UPLOAD_DIR="${FILE_UPLOAD_DIR:-/data/uploads}"

# Aguarda o volume ser montado pelo Railway via /proc/mounts
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
  echo "Timeout aguardando volume. Continuando..."
fi

# Cria subdiretórios (rodando como root, tem permissão total no volume)
mkdir -p "${UPLOAD_DIR}/arquivos-gerais" "${UPLOAD_DIR}/recibos" /app/logs
echo "Diretórios criados em ${UPLOAD_DIR}"

exec java \
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=60.0 \
  -XX:InitialRAMPercentage=30.0 \
  -XX:+UseSerialGC \
  -XX:MaxMetaspaceSize=128m \
  -Djava.security.egd=file:/dev/./urandom \
  -Dspring.jmx.enabled=false \
  -Dspring.profiles.active=prod \
  -jar app.jar "$@"

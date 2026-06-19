#!/bin/sh
set -e

UPLOAD_DIR="${FILE_UPLOAD_DIR:-/data/uploads}"

# Aguarda o volume ser montado pelo Railway (via /proc/mounts)
# O Railway monta volumes após o container iniciar
echo "Aguardando volume em ${UPLOAD_DIR}..."
i=0
while [ $i -lt 30 ]; do
  if grep -q "${UPLOAD_DIR}" /proc/mounts 2>/dev/null; then
    echo "Volume montado em ${UPLOAD_DIR}"
    break
  fi
  sleep 1
  i=$((i + 1))
done

if [ $i -eq 30 ]; then
  echo "Timeout - usando diretório sem volume persistente"
fi

# Cria subdiretórios — roda como root
mkdir -p "${UPLOAD_DIR}/arquivos-gerais" "${UPLOAD_DIR}/recibos" /app/logs
echo "Diretórios prontos em ${UPLOAD_DIR}"

exec java \
  -XX:+UseContainerSupport \
  -Xmx256m \
  -Xms64m \
  -XX:MaxMetaspaceSize=96m \
  -XX:+UseSerialGC \
  -XX:CompressedClassSpaceSize=32m \
  -Djava.security.egd=file:/dev/./urandom \
  -Dspring.jmx.enabled=false \
  -Dspring.profiles.active=prod \
  -Dserver.port="${PORT:-8080}" \
  -jar app.jar "$@"

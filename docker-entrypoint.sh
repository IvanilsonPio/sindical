#!/bin/sh
# Garante que os diretórios existem mesmo após montagem de volumes
mkdir -p "${FILE_UPLOAD_DIR:-/app/uploads}/arquivos-gerais"
mkdir -p /app/logs

exec java \
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 \
  -XX:+UseG1GC \
  -Djava.security.egd=file:/dev/./urandom \
  -Dspring.jmx.enabled=false \
  -Dspring.profiles.active=prod \
  -jar app.jar "$@"

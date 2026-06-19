#!/bin/sh
set -e

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

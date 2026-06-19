#!/bin/sh
set -e

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

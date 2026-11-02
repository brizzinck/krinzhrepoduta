#!/bin/sh
# Запуск OWASP Threat Dragon v2.2.0 (веб-версія) локально в docker: http://localhost:3000
# Використання:  threat-dragon/run.sh start | stop
# Секрети генеруються при першому запуску в threat-dragon/td.env (файл не зберігається в репозиторії).
set -eu
cd "$(dirname "$0")"
NAME=td_lr1
case "${1:-start}" in
  start)
    if [ ! -f td.env ]; then
      rnd() { head -c 32 /dev/urandom | od -An -tx1 | tr -d ' \n' | head -c "$1"; }
      cat > td.env <<ENV
NODE_ENV=production
PORT=3000
SERVER_API_PROTOCOL=http
LOG_LEVEL=info
ENCRYPTION_JWT_SIGNING_KEY=$(rnd 48)
ENCRYPTION_JWT_REFRESH_SIGNING_KEY=$(rnd 48)
ENCRYPTION_KEYS=[{"isPrimary": true, "id": 0, "value": "$(rnd 32)"}]
ENV
    fi
    docker rm -f "$NAME" >/dev/null 2>&1 || true
    docker run -d --name "$NAME" --platform linux/x86_64 -p 127.0.0.1:3000:3000 \
      -e ENV_FILE=/app/td.env -v "$PWD/td.env":/app/td.env:ro owasp/threat-dragon:v2.2.0
    ;;
  stop)
    docker rm -f "$NAME"
    ;;
esac

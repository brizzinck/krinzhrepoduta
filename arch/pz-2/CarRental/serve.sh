#!/bin/sh
# Запуск API у docker (SDK-образ) без встановлення .NET у систему.
# Використання: ./serve.sh start | stop | restart | logs | status | reset
# БД лежить у ./.data (переживає перезапуск контейнера), API слухає http://127.0.0.1:5080.
set -eu
cd "$(dirname "$0")"
NAME=carrental-api
IMAGE=mcr.microsoft.com/dotnet/sdk:10.0
PORT=${PORT:-5080}

run() {
  mkdir -p .data
  docker volume inspect duikt-nuget >/dev/null 2>&1 || docker volume create duikt-nuget >/dev/null
  docker network inspect arch >/dev/null 2>&1 || docker network create arch >/dev/null
  docker run -d --name "$NAME" --network arch -u "$(id -u):$(id -g)" \
    -e HOME=/tmp -e DOTNET_CLI_HOME=/tmp -e NUGET_PACKAGES=/nuget \
    -e DOTNET_NOLOGO=1 -e DOTNET_CLI_TELEMETRY_OPTOUT=1 \
    -e CarRental__DatabasePath=/data/carrental.db -e CarRental__AuditLogPath=/data/audit.log \
    -e ASPNETCORE_HTTP_PORTS= -e ASPNETCORE_ENVIRONMENT="${ASPNETCORE_ENVIRONMENT:-Production}" \
    ${EXTRA_ENV:-} \
    -v duikt-nuget:/nuget -v "$PWD":/w -v "$PWD/.data":/data -w /w \
    -p "127.0.0.1:$PORT:5080" "$IMAGE" \
    dotnet run --project src/CarRental.WebApi --no-launch-profile --urls http://0.0.0.0:5080 >/dev/null
  printf 'очікуємо запуск'
  for _ in $(seq 1 90); do
    if curl -fs "http://127.0.0.1:$PORT/" >/dev/null 2>&1; then echo ' готово'; return 0; fi
    printf '.'; sleep 1
  done
  echo ' не запустилось'; docker logs --tail 30 "$NAME"; return 1
}

case "${1:-}" in
  start)   docker rm -f "$NAME" >/dev/null 2>&1 || true; run ;;
  stop)    docker rm -f "$NAME" >/dev/null 2>&1 || true; echo "зупинено" ;;
  restart) docker rm -f "$NAME" >/dev/null 2>&1 || true; run ;;
  reset)   docker rm -f "$NAME" >/dev/null 2>&1 || true; rm -rf .data; echo "БД видалено" ;;
  logs)    docker logs "$NAME" 2>&1 ;;
  status)  docker ps --filter "name=$NAME" --format '{{.Names}}\t{{.Status}}\t{{.Ports}}' ;;
  *) echo "використання: $0 start|stop|restart|reset|logs|status" >&2; exit 2 ;;
esac

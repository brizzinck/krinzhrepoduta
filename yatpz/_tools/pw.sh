#!/bin/sh
# Запуск Python-скрипта Playwright у docker (headless). Використання: _tools/pw.sh script.py [арг...]
# Поточна тека монтується як /w. Образ збирається один раз: docker build -t duikt-pw _tools/pw
set -eu
HERE=$(cd "$(dirname "$0")" && pwd)
docker image inspect duikt-pw >/dev/null 2>&1 || docker build -q -t duikt-pw "$HERE/pw" >/dev/null
exec docker run --rm -u "$(id -u):$(id -g)" -e HOME=/tmp --ipc=host -v "$PWD":/w -w /w duikt-pw python3 "$@"

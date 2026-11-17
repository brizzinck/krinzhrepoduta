#!/usr/bin/env bash
# Запуск автоматичних перевірок прототипу в контейнері Playwright (Python, Chromium).
# Приклад: homework/digital_tech/prototype/tests/run.sh checks.py
# Тека tests монтується як /s (скрипти, кеш плиток har/), тека прототипу як /p (лише читання).
set -euo pipefail
T="$(cd "$(dirname "$0")" && pwd)"
P="$(cd "$T/.." && pwd)"
SCRIPT="${1:?скрипт: checks.py}"; shift || true
FONT=()
[ -d /usr/share/fonts/Adwaita ] && FONT=(-v /usr/share/fonts/Adwaita:/usr/share/fonts/Adwaita:ro)
docker run --rm --ipc=host -u "$(id -u):$(id -g)" -e HOME=/tmp \
  -v "$P":/p:ro -v "$T":/s -w /p "${FONT[@]}" \
  -v "$T/fonts.conf":/etc/fonts/local.conf:ro \
  lr-shots:pw1.64.1 python "/s/$SCRIPT" "$@"

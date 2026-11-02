#!/bin/sh
# Створює контейнер duikt-unix заново (образ duikt-unix має бути зібраний). З --clean видаляє і том домашнього каталогу.
docker rm -f duikt-unix >/dev/null 2>&1
[ "$1" = "--clean" ] && docker volume rm duikt-unix-home >/dev/null 2>&1
docker run -d --name duikt-unix --init --hostname duikt-unix -v duikt-unix-home:/home/lavrishko duikt-unix sleep infinity

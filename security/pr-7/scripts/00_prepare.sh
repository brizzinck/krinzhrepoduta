#!/bin/bash
# Копіює застосунки в робочу теку збірки й готує том кешу Maven.
set -eu
. "$(dirname "$0")/env.sh"
mkdir -p "$BUILD" "$CACHE" "$RES"
for n in lr-3 lr-6; do
  rm -rf "$BUILD/$n"
  mkdir -p "$BUILD/$n"
  # копія без target і logs: вихідні теки ЛР не змінюються
  (cd "$HW/security/$n/app" && tar --exclude=./target --exclude=./logs -cf - .) | tar -xf - -C "$BUILD/$n"
  echo "копія $n: $BUILD/$n"
done
if ! docker volume inspect "$M2_VOL" >/dev/null 2>&1; then
  docker volume create "$M2_VOL" >/dev/null
  docker run --rm --name cw-pr7-chown -v "$M2_VOL":/m alpine:3 chown "$UIDGID" /m
  echo "створено том $M2_VOL"
fi

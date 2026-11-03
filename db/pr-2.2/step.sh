#!/bin/sh
# Виконує один крок SQL-файла. Крок починається рядком "-- назва. опис" і триває до наступного такого рядка.
# Текст нумерованих кроків показується рядками "# ...".
# Використання: ./step.sh 02   або   ./step.sh equivalence part2_queries.sql
set -eu
cd "$(dirname "$0")"
FILE=${2:-part1_test_index.sql}
SQL=$(awk -v h="-- $1." 'index($0, h) == 1 { on = 1; next } /^-- [0-9a-z_]+\./ { on = 0 } on' "$FILE")
[ -n "$SQL" ] || { echo "у $FILE немає кроку $1" >&2; exit 1; }
case "$1" in
  [0-9]*) printf '%s\n' "$SQL" | sed 's/^/# /' ;;
esac
printf '%s\n' "$SQL" | ../_shared/mysql.sh -t -n --force northwind_opt

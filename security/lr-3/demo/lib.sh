#!/bin/bash
# Спільні функції демонстраційних скриптів. Усі запити йдуть лише на локальний навчальний застосунок.
BASE="${BASE:-http://localhost:8080}"
DBEXEC='docker exec rental_mysql mysql -urental_app -prental_app_pass rental --default-character-set=utf8mb4 -t'

# Корисна частина HTML-відповіді з повідомленнями форми: «поле: текст»
form_errors() {
  python3 -I -c '
import re,sys,html
t=sys.stdin.read()
for m in re.finditer(r"<span class=\"err\" data-for=\"(\w+)\">((?:<span>[^<]*</span>|<br>)*)</span>", t):
    msgs=re.findall(r"<span>(.*?)</span>", m.group(2))
    if msgs: print("  %-10s %s" % (m.group(1), " | ".join(html.unescape(x) for x in msgs)))
'
}

# post_form "опис" поле=значення ...  : POST форми без браузера; друкує статус і повідомлення по полях
post_form() {
  local title="$1"; shift
  local args=()
  for kv in "$@"; do args+=(--data-urlencode "$kv"); done
  echo
  echo "# $title"
  local shown; shown=$(printf -- '-d %q ' "$@")
  [ ${#shown} -gt 260 ] && shown="${shown:0:200} ... [скорочено, усього ${#shown} символів]"
  echo "\$ curl -s -X POST $BASE/register $shown"
  local out; out=$(curl -s -w '\nHTTP_STATUS=%{http_code}' -X POST "$BASE/register" "${args[@]}")
  echo "  HTTP $(echo "$out" | sed -n 's/^HTTP_STATUS=//p')"
  echo "$out" | form_errors
}

# сторінка з таблицею результатів: рядки таблиці у вигляді тексту
table_rows() {
  python3 -I -c '
import re,sys,html
t=sys.stdin.read()
for r in re.findall(r"<tr>(.*?)</tr>", t, re.S):
    cells=re.findall(r"<td>(.*?)</td>", r, re.S)
    if cells: print("  | " + " | ".join(html.unescape(c) for c in cells))
'
}

sql_line() { python3 -I -c '
import re,sys,html
t=sys.stdin.read()
m=re.search(r"Виконано SQL: <code>(.*?)</code>", t, re.S)
print("  SQL: "+html.unescape(m.group(1))) if m else None
'; }

#!/bin/bash
# Строк дії: токен працює одразу після видачі й перестає працювати через 2 хвилини (чекаємо 121 с по-справжньому).
. "$(dirname "$0")/lib.sh"

echo '$ date -u +%H:%M:%S'
date -u +%H:%M:%S
http "Вхід" -X POST "$BASE/api/auth/login" -H "$JSON" -d '{"login":"olena_k","password":"Secret2026"}'
TOKEN=$(token_from_body "$LAST_BODY")
http "Профіль одразу після входу" "$BASE/api/profile" -H "Authorization: Bearer $TOKEN"

echo
echo '$ sleep 121'
sleep 121
echo '$ date -u +%H:%M:%S'
date -u +%H:%M:%S
http "Той самий токен через 121 секунду" "$BASE/api/profile" -H "Authorization: Bearer $TOKEN"

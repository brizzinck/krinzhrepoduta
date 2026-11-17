#!/bin/bash
# Запити без токена й з підробленими токенами: сервер відповідає 401 з причиною.
. "$(dirname "$0")/lib.sh"

TOKEN=$(get_token olena_k Secret2026)

http "Без токена" "$BASE/api/profile"
http "Змінено вміст токена (sub=manager), підпис старий" "$BASE/api/profile" -H "Authorization: Bearer $(jwt_tamper "$TOKEN")"
http "Алгоритм none, підпису немає" "$BASE/api/profile" -H "Authorization: Bearer $(jwt_alg_none "$TOKEN")"
http "Сміття замість токена" "$BASE/api/profile" -H "Authorization: Bearer not-a-token"
# Нормалізація шляху: curl надсилає шлях як є, але сервер зводить його до /api/profile
http "Шлях з параметром /api;x/profile без токена" --path-as-is "$BASE/api;x/profile"
http "Закодований шлях /%61pi/profile без токена" --path-as-is "$BASE/%61pi/profile"

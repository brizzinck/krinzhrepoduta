#!/bin/bash
# Вхід за логіном і паролем, токен на 2 хвилини, доступ до захищеного ендпоінта з токеном.
. "$(dirname "$0")/lib.sh"

http "Неправильний пароль" -X POST "$BASE/api/auth/login" -H "$JSON" -d '{"login":"olena_k","password":"wrong-password"}'
http "Невідомий користувач: та сама відповідь" -X POST "$BASE/api/auth/login" -H "$JSON" -d '{"login":"nobody","password":"wrong-password"}'

http "Правильний логін і пароль: токен у відповіді (скорочено)" -X POST "$BASE/api/auth/login" -H "$JSON" -d '{"login":"olena_k","password":"Secret2026"}'
TOKEN=$(token_from_body "$LAST_BODY")

echo
echo "# Вміст токена (base64url, не шифрування)"
echo '$ jwt_decode "$TOKEN"'
jwt_decode "$TOKEN"

http "Захищений ендпоінт з токеном" "$BASE/api/profile" -H "Authorization: Bearer $TOKEN"

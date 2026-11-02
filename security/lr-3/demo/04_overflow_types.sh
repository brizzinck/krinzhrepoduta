#!/bin/bash
# Переповнення, довжини, неправильні типи й формати.
. "$(dirname "$0")/lib.sh"
TMP="${TMPDIR:-/tmp}/lr3-demo"; mkdir -p "$TMP"
LONG10K=$(head -c 10000 /dev/zero | tr '\0' 'a')
OK=(firstName=Олена email=o@example.com password=Secret123 phone=+380501234567 birthDate=1998-04-12)

echo "Переповнення і довжина:"
echo
echo "# Вразлива реєстрація: логін 10 000 символів, поле БД VARCHAR(32). Результат: 500 і подробиці помилки"
echo "\$ curl -s -X POST $BASE/demo/vuln/register -d login=<a x10000> ... | head -c 420"
out=$(curl -s -w '\nHTTP_STATUS=%{http_code}' -X POST "$BASE/demo/vuln/register" --data-urlencode "login=$LONG10K" \
  --data-urlencode "firstName=Олена" --data-urlencode "email=o@example.com" --data-urlencode "password=x" \
  --data-urlencode "phone=1" --data-urlencode "birthDate=2000-01-01")
echo "${out:0:420} ... [скорочено]"
echo "  HTTP $(echo "$out" | sed -n 's/^HTTP_STATUS=//p')"

post_form "Безпечна форма: логін 10 000 символів" login="$LONG10K" "${OK[@]}"
echo
echo "# Безпечна форма: тіло запиту ~5 МБ (ліміт 16 КБ)"
python3 -I -c 'print("login=" + "a"*5_000_000 + "&firstName=x", end="")' > "$TMP/big.txt"
ls -l "$TMP/big.txt" | awk '{print "  розмір тіла: " $5 " байтів"}'
echo "\$ curl -s -X POST $BASE/register --data-binary @big.txt -H 'Content-Type: application/x-www-form-urlencoded'"
curl -s -w '\n  HTTP %{http_code}\n' -X POST "$BASE/register" -H 'Content-Type: application/x-www-form-urlencoded' --data-binary @"$TMP/big.txt"
echo
echo "# Безпечний JSON-ендпоінт: так само 413"
python3 -I -c 'print("{\"login\":\"" + "a"*5_000_000 + "\"}", end="")' > "$TMP/big.json"
curl -s -w '\n  HTTP %{http_code}\n' -X POST "$BASE/api/register" -H 'Content-Type: application/json' --data-binary @"$TMP/big.json"

post_form "Дата-переповнення: birthDate=99999999999999999999" login=client01 "${OK[@]:0:4}" birthDate=99999999999999999999

echo
echo "Числове переповнення (int):"
echo
echo "# Вразливо: ручний parseInt без обробки помилки"
echo "\$ curl -s -w '  HTTP %{http_code}' '$BASE/demo/vuln/page?n=2147483648' | head -c 330"
curl -s -w '\n  HTTP %{http_code}\n' "$BASE/demo/vuln/page?n=2147483648" | head -c 330; echo " ..."
echo
echo "# Безпечно: параметр типу int, помилка типу дає 400 без внутрішніх подробиць"
for n in 2147483648 -2147483649 abc 1.5 "" 42; do
  printf '  n=%s: ' "$n"
  curl -s -w ' [HTTP %{http_code}]\n' "$BASE/demo/safe/page?n=$n"
done

echo
echo "Неправильні типи й формати:"
post_form "birthDate=abc" login=client01 "${OK[@]:0:4}" birthDate=abc
post_form "birthDate=2099-01-01 (майбутнє)" login=client01 "${OK[@]:0:4}" birthDate=2099-01-01
post_form "birthDate=1850-01-01 (занадто давно)" login=client01 "${OK[@]:0:4}" birthDate=1850-01-01
post_form "phone=abc" login=client01 firstName=Олена email=o@example.com password=Secret123 phone=abc birthDate=1998-04-12
post_form "email=a@b (без крапки в домені)" login=client01 firstName=Олена email=a@b password=Secret123 phone=+380501234567 birthDate=1998-04-12
echo
echo "# Нульовий байт у логіні (%00): передаємо вже закодованим, щоб оболонка його не відкинула"
echo "\$ curl -s -X POST $BASE/register -d 'login=client%00x&firstName=Олена&email=o@example.com&...'"
curl -s -w '\n  HTTP_STATUS=%{http_code}\n' -X POST "$BASE/register" -d 'login=client%00x&firstName=%D0%9E%D0%BB%D0%B5%D0%BD%D0%B0&email=o%40example.com&password=Secret123&phone=%2B380501234567&birthDate=1998-04-12' | tr -d '\0' | { out=$(cat); echo "  HTTP $(echo "$out" | sed -n 's/^ *HTTP_STATUS=//p')"; echo "$out" | form_errors; }

echo
echo "# Повторюваний параметр firstName=a&firstName=b (parameter pollution)"
echo "\$ curl -s -X POST $BASE/register -d 'login=client01&firstName=Anna&firstName=Maria&email=o@example.com&...'"
curl -s -w '\n  HTTP_STATUS=%{http_code}\n' -X POST "$BASE/register" -d 'login=client01&firstName=Anna&firstName=Maria&email=o@example.com&password=Secret123&phone=%2B380501234567&birthDate=1998-04-12' | tr -d '\0' | { out=$(cat); echo "  HTTP $(echo "$out" | sed -n 's/^ *HTTP_STATUS=//p')"; echo "$out" | form_errors; }

post_form "Unicode: RTL-символ U+202E у логіні" login=$'client\xe2\x80\xae01' "${OK[@]}"
post_form "Спецсимвол в імені (підкреслення)" login=client01 firstName="Олена_" email=o@example.com password=Secret123 phone=+380501234567 birthDate=1998-04-12
post_form "Unicode: цифри й дефіс в імені (не літери)" login=client01 firstName="Anna-Maria" email=o@example.com password=Secret123 phone=+380501234567 birthDate=1998-04-12
post_form "Пароль із кирилиці довший за 72 байти" login=client01 firstName=Олена email=o@example.com password="Ж1$(printf 'ж%.0s' $(seq 1 40))" phone=+380501234567 birthDate=1998-04-12

echo
echo "# JSON-ендпоінт: неправильні типи значень"
echo "\$ curl -s -X POST $BASE/api/register -H 'Content-Type: application/json' -d '{\"login\":12345,\"birthDate\":\"abc\"}'"
curl -s -w '\n  HTTP %{http_code}\n' -X POST "$BASE/api/register" -H 'Content-Type: application/json' -d '{"login":12345,"birthDate":"abc"}'
echo "\$ curl -s -X POST $BASE/api/register -H 'Content-Type: application/json' -d '{not json'"
curl -s -w '\n  HTTP %{http_code}\n' -X POST "$BASE/api/register" -H 'Content-Type: application/json' -d '{not json'
echo "\$ curl -s -X POST $BASE/api/register ... поля з помилками"
curl -s -w '\n  HTTP %{http_code}\n' -X POST "$BASE/api/register" -H 'Content-Type: application/json' \
  -d '{"login":"abc","firstName":"Ol3na","email":"a@b","password":"short","phone":"abc","birthDate":"2099-01-01"}'
rm -rf "$TMP"

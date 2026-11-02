#!/bin/bash
# XSS: вразлива сторінка виводить введене як HTML (th:utext), безпечна екранує (th:text) і має CSP.
. "$(dirname "$0")/lib.sh"
PAYLOAD='<script>alert(1)</script>'

grab() { # режим: показати, як payload потрапив у HTML-відповідь
  curl -s -G "$BASE/demo/$1/search" --data-urlencode "brand=$PAYLOAD" | grep -F 'Ви шукали' | sed 's/^ *//'
}

echo "# Вразлива сторінка: payload потрапляє в HTML як є, браузер виконає скрипт"
echo "\$ curl -s -G $BASE/demo/vuln/search --data-urlencode 'brand=$PAYLOAD' | grep 'Ви шукали'"
grab vuln
echo
echo "# Безпечна сторінка: спецсимволи перетворено на сутності, скрипт лишається текстом"
echo "\$ curl -s -G $BASE/demo/safe/search --data-urlencode 'brand=$PAYLOAD' | grep 'Ви шукали'"
grab safe

echo
echo "# Заголовки: вразлива сторінка без CSP, безпечна має CSP, що забороняє inline-скрипти"
echo "\$ curl -sI '$BASE/demo/vuln/search?brand=x' | grep -i -E 'HTTP/|content-security|x-content-type'"
curl -sI "$BASE/demo/vuln/search?brand=x" | grep -i -E 'HTTP/|content-security|x-content-type' | tr -d '\r'
echo "\$ curl -sI '$BASE/demo/safe/search?brand=x' | grep -i -E 'HTTP/|content-security|x-content-type'"
curl -sI "$BASE/demo/safe/search?brand=x" | grep -i -E 'HTTP/|content-security|x-content-type' | tr -d '\r'

echo
echo "# Збережений XSS: вразлива реєстрація (без валідації) кладе розмітку в БД"
echo "\$ curl -s -X POST $BASE/demo/vuln/register -d 'login=stored2&firstName=<svg onload=alert(1)>&...'"
curl -s -w '  HTTP %{http_code}\n' -X POST "$BASE/demo/vuln/register" \
  --data-urlencode "login=stored2" --data-urlencode "firstName=<svg onload=alert(1)>" \
  --data-urlencode "email=s2@example.com" --data-urlencode "password=x" \
  --data-urlencode "phone=1" --data-urlencode "birthDate=2000-01-01"
echo
echo "# У БД лежить розмітка і пароль у відкритому вигляді (грубі помилки вразливого варіанта)"
$DBEXEC -e "SELECT login, first_name, password_hash FROM users WHERE login='stored2'" 2>/dev/null

post_form "Безпечна форма: той самий payload в імені відхиляється allow-list'ом" login=safe_user firstName="<svg onload=alert(1)>" email=o@example.com password=Secret123 phone=+380501234567 birthDate=1998-04-12
post_form "Безпечна форма: HTML у логіні й телефоні" login="<b>bold</b>" firstName=Олена email=o@example.com password=Secret123 phone="<script>" birthDate=1998-04-12

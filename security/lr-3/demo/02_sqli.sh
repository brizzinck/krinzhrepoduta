#!/bin/bash
# SQL-ін'єкція: вразлива сторінка (конкатенація) і безпечна (параметризований запит).
. "$(dirname "$0")/lib.sh"

search() { # режим, brand
  echo
  echo "# $3"
  echo "\$ curl -s -G $BASE/demo/$1/search --data-urlencode \"brand=$2\""
  local out; out=$(curl -s -G -w '\nHTTP_STATUS=%{http_code}' "$BASE/demo/$1/search" --data-urlencode "brand=$2")
  echo "  HTTP $(echo "$out" | sed -n 's/^HTTP_STATUS=//p')"
  echo "$out" | sql_line
  local rows; rows=$(echo "$out" | table_rows)
  if [ -n "$rows" ]; then echo "$rows"; else echo "  (рядків немає)"; fi
}

echo "Вразливий варіант (до захисту):"
search vuln "Toyota" "Нормальний пошук"
search vuln "' OR '1'='1" "Булева ін'єкція: повертаються всі авто"
search vuln "' UNION SELECT login,password_hash,0,0 FROM users-- " "UNION-ін'єкція: витік логінів і хешів паролів із таблиці users"
echo
echo "# Одинарна лапка ламає запит: помилка розкриває SQL і внутрішні класи"
echo "\$ curl -s -G $BASE/demo/vuln/search --data-urlencode \"brand='\" | head -c 700"
curl -s -G "$BASE/demo/vuln/search" --data-urlencode "brand='" | head -c 700; echo " ..."

echo
echo "Безпечний варіант (після захисту):"
search safe "Toyota" "Нормальний пошук"
search safe "' OR '1'='1" "Той самий рядок: шукається бренд із такою назвою, збігів немає"
search safe "' UNION SELECT login,password_hash,0,0 FROM users-- " "Той самий UNION: просто текст параметра"
search safe "'" "Одинарна лапка: звичайний порожній результат, без помилки"

post_form "Форма реєстрації: SQL-ін'єкція в полі login (allow-list [A-Za-z0-9_.-])" login="x' OR '1'='1" firstName=Олена email=o@example.com password=Secret123 phone=+380501234567 birthDate=1998-04-12
post_form "Форма реєстрації: DROP TABLE у полі login" login="a'; DROP TABLE users;--" firstName=Олена email=o@example.com password=Secret123 phone=+380501234567 birthDate=1998-04-12

echo
echo "# Таблиця users на місці, DROP не виконано"
echo "\$ mysql ... -e \"SELECT COUNT(*) AS users_total FROM users\""
$DBEXEC -e "SELECT COUNT(*) AS users_total FROM users" 2>/dev/null

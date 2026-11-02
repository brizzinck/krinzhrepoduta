#!/bin/bash
# Повний прогін: збірка, тести, чистий стенд (MySQL + застосунок у docker), скрипти атак, знімки.
# Запуск з теки роботи:  demo/run_all.sh
set -u
cd "$(dirname "$0")/.."
TOOLS=$(cd ../../tools && pwd)

echo "Збірка і тести:"
(cd app && $TOOLS/mvn.sh test 2>&1 | grep -E "Tests run:|BUILD|WARN.*Tests" > ../demo/05_tests_output.txt; cat ../demo/05_tests_output.txt | tail -3)
(cd app && $TOOLS/mvn.sh -q -DskipTests package)

echo "Чистий стенд:"
docker compose -f app/compose.yaml down -v >/dev/null 2>&1
docker compose -f app/compose.yaml up -d >/dev/null 2>&1
for i in $(seq 1 60); do
  curl -s -o /dev/null http://localhost:8080/register && break
  sleep 2
done

echo "Скрипти атак:"
for n in 01_client_bypass 02_sqli 03_xss 04_overflow_types; do
  demo/$n.sh > demo/$n.txt 2>&1
  echo "   demo/$n.txt: $(wc -l < demo/$n.txt) рядків"
done

echo "Запис у БД:"
{
  echo "\$ docker exec rental_mysql mysql -urental_app rental -e \"SELECT id, login, first_name, LEFT(password_hash, 7) AS hash_prefix, LENGTH(password_hash) AS hash_len, role FROM users WHERE login IN ('olena_k','attacker1','manager') ORDER BY id\""
  docker exec rental_mysql mysql -urental_app -prental_app_pass rental --default-character-set=utf8mb4 -t \
    -e "SELECT id, login, first_name, LEFT(password_hash, 7) AS hash_prefix, LENGTH(password_hash) AS hash_len, role FROM users WHERE login IN ('olena_k','attacker1','manager') ORDER BY id" 2>/dev/null
} > demo/06_db_check.txt
cat demo/06_db_check.txt


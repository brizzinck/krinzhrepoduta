#!/bin/bash
# Повний прогін: тести, збірка, запуск у docker, скрипти 01-04, зупинка. Триває понад 3 хвилини (чекання 121 с).
# Запуск з теки роботи:  demo/run_all.sh   (KEEP=1 залишає застосунок запущеним)
set -u
cd "$(dirname "$0")/.."
TOOLS=$(cd ../../tools && pwd)
export HOST_UID=$(id -u) HOST_GID=$(id -g)

echo "Тести:"
(cd app && $TOOLS/mvn.sh test 2>&1 | grep -E "Tests run:|BUILD" > ../demo/05_tests_output.txt; tail -3 ../demo/05_tests_output.txt)
echo "Збірка:"
(cd app && $TOOLS/mvn.sh -q -DskipTests package)

echo "Запуск:"
docker compose -f app/compose.yaml down >/dev/null 2>&1
rm -rf app/logs && mkdir -p app/logs
docker compose -f app/compose.yaml up -d >/dev/null 2>&1
for i in $(seq 1 60); do
  curl -s -o /dev/null http://localhost:8086/ && break
  sleep 2
done

for n in 01_login_access 02_rejected_tokens 03_expiry 04_log; do
  demo/$n.sh > demo/$n.txt 2>&1
  echo "   demo/$n.txt: $(wc -l < demo/$n.txt) рядків"
done

[ "${KEEP:-0}" = 1 ] || docker compose -f app/compose.yaml down >/dev/null 2>&1

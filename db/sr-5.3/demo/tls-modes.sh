#!/bin/sh
# Рис. 2 (SQL у sql/02 і sql/03): вимога TLS на сервері, клієнт без шифрування, відмова VERIFY_IDENTITY без довіреного CA.
# Порядок команд важливий. Перша команда з'єднується з 127.0.0.1 за замовчуванням, тобто по TLS, і цим входом
# root@% потрапляє в кеш caching_sha2_password. Лише після цього сервер приймає вхід без TLS (--ssl-mode=DISABLED).
# На щойно запущеному сервері без першої команди буде ERROR 2061 (Authentication requires secure connection).
cd "$(dirname "$0")/.."
M="./mysql.sh -h 127.0.0.1"
show() { printf '$ %s\n' "$*"; sh -c "$*" 2>&1; }
show "$M -vvv < sql/02-require-secure-transport.sql"
show "$M --ssl-mode=DISABLED -vvv < sql/03-ssl-cipher-no-tls.sql"
URL="jdbc:mysql://127.0.0.1:3306/car_rental?sslMode=VERIFY_IDENTITY"
printf '$ %s\n' "DB_URL='$URL' pr-3.1/run.sh < demo/tls-read.txt | sed -n 1p"
DB_URL="$URL" pr-3.1/run.sh < demo/tls-read.txt | sed -n 1p

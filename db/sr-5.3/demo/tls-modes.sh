#!/bin/sh
# Рис. 2: вимога TLS на сервері, клієнт без шифрування, відмова VERIFY_IDENTITY без довіреного CA.
# Порядок команд важливий. Перша команда з'єднується з 127.0.0.1 за замовчуванням, тобто по TLS, і цим входом
# root@% потрапляє в кеш caching_sha2_password. Лише після цього сервер приймає вхід без TLS (--ssl-mode=DISABLED).
# На щойно запущеному сервері без першої команди буде ERROR 2061 (Authentication requires secure connection).
cd "$(dirname "$0")/.."
M="../_shared/mysql.sh -h 127.0.0.1 -N"
show() { printf '$ %s\n' "$*"; sh -c "$*" 2>&1; }
show "$M -e \"SHOW VARIABLES LIKE 'require_secure_transport'\""
show "$M --ssl-mode=DISABLED -e \"SHOW SESSION STATUS LIKE 'Ssl_cipher'\""
URL="jdbc:mysql://127.0.0.1:3306/car_rental?sslMode=VERIFY_IDENTITY"
printf '$ %s\n' "DB_URL='$URL' ../pr-3.1/run.sh < demo/tls-read.txt | sed -n 1p"
DB_URL="$URL" ../pr-3.1/run.sh < demo/tls-read.txt | sed -n 1p

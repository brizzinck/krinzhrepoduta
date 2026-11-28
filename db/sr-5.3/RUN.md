# СР 5.3: запуск і показ

Потрібен лише Docker. Усі команди з теки `homework/db/sr-5.3`. Хмарний сервер Azure не створювався (потрібна підписка): кроки взято з документації, а TLS-підключення перевірено локально.

## Пайплайн

```sh
cd /home/skalse/job/duikt/course_3/students/lavrishko_vladyslav/homework/db/sr-5.3
make tls        # застосунок по TLS, шифр з сервера, близько 8 секунд
make modes      # без шифрування і VERIFY_IDENTITY
make grants     # за потреби: права користувача rental
```

`make show` виконує `tls` і `modes` підряд. `make help` показує всі цілі. Цілі самі готують базу `car_rental` і jar застосунку ПР 3.1. Порядок у `make modes` важливий: перша команда заходить по TLS і кладе вхід у кеш сервера, без неї друга дала б `ERROR 2061`. Скрипт це робить сам. Другий термінал не потрібен.

## SQL по кроках

- `make tls`: `sql/01-app-session-tls.sql`, шифр і версія TLS з'єднання застосунку (запит до `performance_schema`).
- `make modes`: `sql/02-require-secure-transport.sql`, `sql/03-ssl-cipher-no-tls.sql`, вимога TLS на сервері й шифр без TLS; крок `VERIFY_IDENTITY` запускає застосунок, SQL у ньому немає.
- `make grants`: `sql/04-show-grants.sql`, права користувача `rental`.
- `make sql`: друкує весь вміст `sql/`.

## Що показати

1. `make tls`. Перший рядок `Підключено до БД: car_rental` (застосунок із `sslMode=REQUIRED`). Нижче `rental | Ssl_cipher | TLS_AES_256_GCM_SHA384` і `rental | Ssl_version | TLSv1.3`. З'єднання застосунку зашифроване TLS 1.3, змінився лише рядок підключення.
2. `make modes`. `require_secure_transport OFF`: локально TLS не вимагається (в Azure вимагається). З `--ssl-mode=DISABLED` `Ssl_cipher` порожній, тобто без шифрування. З `sslMode=VERIFY_IDENTITY`: `Не вдалося підключитися до БД: Communications link failure`, бо JVM не довіряє сертифікату сервера, потрібен CA.
3. Таблицю 3 у `report.pdf` (локально проти Azure) відкрити в PDF.
4. За потреби `make grants`: це лише перегляд прав локального користувача. Права в Azure обмежені, `GRANT` на показі не змінювати.

Код: `demo/tls-app.sh`, `demo/tls-modes.sh`, `sql/01-app-session-tls.sql`.

## Звідки що береться

`make` друкує кожну команду завантаження бази, наприклад `./mysql.sh < schema.sql`. `mysql.sh` запускає клієнт `mysql` у контейнері `duikt-mysql`, `mysql-up.sh` піднімає цей контейнер.

- `schema.sql`, `seed.sql`: база `car_rental`, копія з ПР 1.1.
- `pr-3.1/`: копія ПР 3.1 із зібраним jar.

# ЛР 3: запуск і показ

Потрібні Docker (образи `maven:3.9-eclipse-temurin-21` і `mysql:8`, залежності Maven беруться з тома `duikt-m2`), `bash`, `curl` і `python3` на хості (скрипти атак розбирають HTML-відповіді). Усі команди з теки `security/lr-3` репозиторію. Збірка йде в `app/target/`, порівняння виводу в `target/out/`; файли репозиторію (`demo/*.txt`, `img/`, звіт) `make` не змінює. Застосунок стає на порт `PORT` (типово 8080), MySQL у docker без відкритого порту. Якщо порт зайнятий: `make show PORT=8090`.

## Пайплайн

```sh
cd security/lr-3   # шлях відносно кореня репозиторію
make build      # mvn package: app/target/app.jar
make test       # 76 автотестів
make up         # MySQL 8 і застосунок із профілем vuln у docker
make bypass     # атака 1: обхід клієнтської перевірки, mass assignment
make sqli       # атака 2: SQL-ін'єкція до і після захисту
make xss        # атака 3: XSS, екранування, CSP
make overflow   # атака 4: переповнення, типи, формати
make db         # запис у БД: роль CLIENT, BCrypt-хеш
make down       # зупинити стенд
```

Кожна ціль працює й окремо: якщо застосунку ще немає, `make` сам збере jar і підніме стенд (контейнери лишаються, поки не виконано `make down`). Окрема ціль перед атакою повертає таблицю `users` до початкового стану (лишається `manager`), тому її можна повторювати. `make show` піднімає чистий стенд один раз і виконує атаки по черзі, як при зніманні `demo/*.txt` (атака 2 бачить `attacker1` з атаки 1), і зупиняє контейнери в кінці; `make help` показує цілі, `make clean` видаляє контейнери й тимчасові файли. Інший профіль: `make up APP_PROFILE=default` (без вразливих сторінок `/demo/vuln/*`).

## Кроки

- `build`: `../../tools/mvn.sh -DskipTests package` у `app/`.
- `test`: `mvn test` у `app/` (JUnit 5, MockMvc, H2 у режимі MySQL; Docker для БД не потрібен).
- `up`: контейнери `lr3-mysql` (`mysql:8`, схема з `app/sql/init.sql`) і `lr3-app` (`app/target/app.jar`, змінна `SPRING_PROFILES_ACTIVE`) у мережі `lr3-net`; назви задає `NAME`.
- `bypass`: `demo/01_client_bypass.sh`, запити `curl` на `POST /register` без браузера.
- `sqli`: `demo/02_sqli.sh`, пошук за брендом на `/demo/vuln/search` (конкатенація) і `/demo/safe/search` (параметр `?`), потім форма реєстрації.
- `xss`: `demo/03_xss.sh`, ті самі сторінки з `<script>alert(1)</script>`, заголовки CSP, збережений XSS. Виконання скрипту в браузері через `make` не відтворюється: це зроблено окремо в браузері (Playwright), протокол лежить у `demo/xss-dialogs.txt` і друкується в кінці кроку.
- `overflow`: `demo/04_overflow_types.sh`, логін 10 000 символів, тіло 5 МБ, chunked, `int` на `/demo/*/page`, неправильні типи й формати.
- `db`: `SELECT` з `users` для `manager`, `attacker1`, `olena_k` (окремо ще реєструє `attacker1` із зайвим полем `role=ADMIN`; у `make show` він уже зареєстрований кроком `bypass`).

Після кожної атаки `make` порівнює свіжий вивід (порт, час у відповідях і випадкова сіль BCrypt не враховуються) з `demo/NN_*.txt`, який використано в звіті, і друкує «збігається» або різницю.

## Що показати

1. `make build`: `BUILD SUCCESS`, файл `app/target/app.jar`.
2. `make test`: `Tests run: 76, Failures: 0, Errors: 0, Skipped: 0` і `BUILD SUCCESS`.
3. `make bypass`: порожня форма й некоректні значення дають `HTTP 400` і повідомлення по кожному полю (`login      Вкажіть логін`, `birthDate  Дата народження не може бути в майбутньому`). Запит з `role=ADMIN` дає `HTTP 302`, а в БД `attacker1 | CLIENT | $2a$10$`. Кінець: `Вивід збігається з demo/01_client_bypass.txt`.
4. `make sqli`: на `/demo/vuln/search` `' OR '1'='1` повертає всі п'ять авто, `UNION` додає рядок `manager | $2a$10$OMIQiy/8WhB.wovMRigmk.9.9ohWa7lmSvjiVjdyDI.m1vhycZciK`, одинарна лапка дає JSON з `BadSqlGrammarException`. На `/demo/safe/search` ті самі рядки дають `HTTP 200` і `(рядків немає)`. У формі `login="x' OR '1'='1"` і `DROP TABLE` дають `HTTP 400`, `users_total` дорівнює 2 (`manager` і `attacker1`). У UNION-відповіді є і `attacker1`. Кінець: `Вивід збігається з demo/02_sqli.txt`. Окремо запущена ціль `make sqli` починає з чистої таблиці, тож без рядка `attacker1` і з `users_total` 1: вона сама друкує цю різницю й причину.
5. `make xss`: вразлива сторінка повертає `<strong><script>alert(1)</script></strong>`, безпечна `<strong>&lt;script&gt;alert(1)&lt;/script&gt;</strong>`; лише в безпечної є `Content-Security-Policy: default-src 'self'; script-src 'self'; ...`. Безпечна форма відхиляє `<svg onload=alert(1)>` (`HTTP 400`). Кінець: `Вивід збігається з demo/03_xss.txt` і протокол браузера з діалогами `alert`.
6. `make overflow`: вразлива реєстрація з логіном із 10 000 символів дає `HTTP 500`, безпечна `HTTP 400`; тіло 5000018 байтів (звичайне й chunked) дає `HTTP 413`; `n=2147483648` на безпечній сторінці `HTTP 400`, `n=42` `{"page":42}`; `text/plain` дає `HTTP 415`, `DELETE` дає `HTTP 405`. Кінець: `Вивід збігається з demo/04_overflow_types.txt`.
7. `make db` після інших кроків: таблиця `manager | ADMIN` і `attacker1 | CLIENT`, обидва хеші `$2a$10$` довжиною 60 (ті самі рядки, що в `demo/06_db_check.txt`; `make` їх не порівнює). Окремо `make db` спершу друкує `Реєстрація attacker1 із role=ADMIN: HTTP 302`.

## Звідки що береться

- `app/src/main/java/ua/duikt/lavrishko/rental/`: `dto/RegistrationForm`, `validation/`, `web/`, `repo/`, `demo/VulnerableDemoController` і `SafeDemoController`, `config/RequestGuardFilter`.
- `app/sql/init.sql`: схема й початкові дані (п'ять авто, користувач `manager`).
- `app/compose.yaml`: той самий стенд для запуску вручну (`docker compose up -d`).
- `demo/*.sh`: скрипти атак, `demo/lib.sh`: спільні функції; `demo/*.txt`: вивід, вставлений у звіт.
- `img/`: знімки до рисунків звіту (`t01` для атаки 1, `t03` і `t04` для SQL-ін'єкції, `t06` для XSS, `t08` для переповнення, `t12` для тестів), у `make` не використовуються.

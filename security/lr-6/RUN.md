# ЛР 6: запуск і показ

Потрібні Docker (образи `maven:3.9-eclipse-temurin-21` і `eclipse-temurin:21-jre`, залежності Maven беруться з тома `duikt-m2`), `bash`, `curl` і `python3` на хості (скрипти розбирають і підробляють токени). Усі команди з теки `security/lr-6` репозиторію. Збірка йде в `app/target/`, журнал у `app/logs/`; файли репозиторію (`demo/*.txt`, `img/`, звіт) `make` не змінює. Застосунок стає на порт `PORT` (типово 8086), якщо він зайнятий: `make show PORT=8090`. Секрет підпису щоразу випадковий (змінна `JWT_SECRET` не задана), тож токени з різних запусків відрізняються.

## Пайплайн

```sh
cd security/lr-6   # шлях відносно кореня репозиторію
make build      # mvn package: app/target/app.jar
make test       # 18 автотестів
make up         # застосунок у docker, форма входу на http://localhost:8086/
make login      # вхід, токен на 120 с, профіль із токеном
make rejected   # токени, які сервер відхиляє
make expiry     # строк дії: чекання 121 секунда (триває понад 2 хвилини)
make log        # журнал невдалих спроб
make down       # зупинити застосунок
```

Кожна ціль працює й окремо: якщо застосунку ще немає, `make` сам збере jar і запустить контейнер (він лишається, поки не виконано `make down`). `make log` спершу сам виконує `login` і `rejected`, щоб журнал не був порожнім; рядок `reason=expired` з'являється в ньому лише після `make expiry`. `make show` виконує все по черзі (понад 3 хвилини) й зупиняє контейнер, `make help` показує цілі, `make clean` зупиняє контейнер і видаляє `app/logs`. Поки застосунок працює, у браузері відкривається форма: `olena_k` / `Secret2026`.

## Кроки

- `build`: `../../tools/mvn.sh -DskipTests package` у `app/`.
- `test`: `mvn test` у `app/` (`TokenAuthTest`, годинник підміняється, тож строк 120 с перевіряється без очікування).
- `up`: контейнер `lr6-app` (`NAME`) запускає `java -jar /opt/app.jar`; `app/logs` змонтовано як `/work/logs`, звідти беруться рядки журналу.
- `login`: `demo/01_login_access.sh`, `POST /api/auth/login` (хибний пароль, невідомий логін, правильна пара), розбір токена (`jwt_decode` у `demo/lib.sh`) і `GET /api/profile` з заголовком Bearer.
- `rejected`: `demo/02_rejected_tokens.sh`, запити без токена, зі зміненим вмістом (`jwt_tamper`), з `alg=none` (`jwt_alg_none`), зі сміттям і шляхами `/api;x/profile`, `/%61pi/profile`. Токени підробляються локально, зі справжнього токена.
- `expiry`: `demo/03_expiry.sh`, вхід, профіль одразу, `sleep 121`, той самий токен ще раз.
- `log`: `demo/04_log.sh`, вміст `app/logs/auth-failures.log` і `grep -c eyJ`.

## Що показати

1. `make build`: `BUILD SUCCESS`, файл `app/target/app.jar`.
2. `make test`: `Tests run: 18, Failures: 0, Errors: 0, Skipped: 0` і `BUILD SUCCESS`.
3. `make login`: хибний пароль і невідомий логін дають однакову відповідь `{"error":"invalid_credentials"}`, `HTTP 401`. Правильна пара дає `HTTP 200` і `"expiresIn":120`. У токені `header: {"alg": "HS256"}` і `exp - iat = 120 с`. Профіль з токеном: `HTTP 200`, `{"login":"olena_k","role":"CLIENT",...,"secondsLeft":120}`.
4. `make rejected`: `HTTP 401` з причинами `missing` (без токена), `bad_signature` (змінено `sub=manager`), `wrong_alg` (`alg=none`), `malformed` (`not-a-token`); шляхи `/api;x/profile` і `/%61pi/profile` без токена теж дають `missing`.
5. `make expiry`: профіль одразу `HTTP 200`, після `sleep 121` (позначки часу різняться на 121 секунду) той самий токен дає `{"error":"invalid_token","reason":"expired"}`, `HTTP 401`.
6. `make log`: рядки `LOGIN_FAILED login=olena_k`, `LOGIN_FAILED login=nobody` і шість `TOKEN_REJECTED` (`missing`, `bad_signature`, `wrong_alg`, `malformed`, ще два `missing` зі шляхами `/api?x/profile` і `/?61pi/profile`), після `make expiry` сьомий з `reason=expired`. Далі `0` (повних токенів у журналі немає) і рядок `grep не знайшов жодного eyJ: повних токенів у журналі немає (код 1 означає нуль збігів)`. Якщо токен у журналі є, `make` друкує `УВАГА: у журналі знайдено повний токен, рядків з eyJ: N` і завершується з помилкою.

Вивід збігається зі збереженим у `demo/*.txt` за структурою; час, токени, `jti` і відбитки щоразу інші.

## Звідки що береться

- `app/src/main/java/ua/duikt/lavrishko/rental/auth/`: `TokenService` (видача й перевірка), `JwtAuthFilter`, `AuthController`, `ProfileController`, `AuditLog`, `UserStore`.
- `app/src/main/resources/`: `application.properties`, `logback-spring.xml`, форма входу в `static/`.
- `app/compose.yaml`: той самий стенд для запуску вручну.
- `demo/*.sh`: скрипти, `demo/lib.sh`: спільні функції; `demo/*.txt`: вивід, вставлений у звіт.
- `img/`: знімки до рисунків звіту; у `make` не використовуються.

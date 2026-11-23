# ЛР 3: валідація вхідних даних (форма реєстрації, SQL-ін'єкція, XSS, переповнення). Повний показ: make show
.RECIPEPREFIX := >
SHELL := /bin/bash
.SHELLFLAGS := -o pipefail -c
.PHONY: help build test up ready bypass sqli xss overflow db down clean show steps
T := ../../tools
PORT ?= 8080
NAME ?= lr3
APP_PROFILE ?= vuln
# FRESH=1: кожна ціль сама повертає таблицю users до початкового стану; show ставить FRESH=0 і чистить її один раз при запуску стенду
FRESH ?= 1
BASE := http://localhost:$(PORT)
DBC := $(NAME)-mysql
APPC := $(NAME)-app
NET := $(NAME)-net
JAR := app/target/app.jar
IMG := maven:3.9-eclipse-temurin-21
DBSH := docker exec $(DBC) mysql -urental_app -prental_app_pass rental --default-character-set=utf8mb4 -t
export BASE
export DB_CONTAINER := $(DBC)

# Порівняння свіжого виводу зі збереженим файлом, який використано в звіті (порт, час у відповідях і випадкова сіль BCrypt не враховуються)
NORM = sed -e 's|localhost:$(PORT)|localhost:8080|g' -e 's|"timestamp":"[^"]*"|"timestamp":"T"|g' -e 's|\$$2a\$$10\$$[A-Za-z0-9./]\{53\}|HASH|g'
# Окрема ціль перед атакою повертає таблицю users до початкового стану (лишається manager), щоб кроки повторювалися
RESET = $(if $(filter 1,$(FRESH)),@docker exec $(DBC) mysql -urental_app -prental_app_pass rental -e "DELETE FROM users WHERE login <> 'manager'; ALTER TABLE users AUTO_INCREMENT = 2" 2>/dev/null,@:)

define CMP
@if diff -q <($(NORM) demo/$(1).txt) <($(NORM) target/out/$(1).txt) >/dev/null; then echo; echo "Вивід збігається з demo/$(1).txt (його використано в звіті; порт, час і сіль BCrypt не враховано)."; else echo; echo "Відмінності від demo/$(1).txt (< у звіті, > зараз):"; { diff <($(NORM) demo/$(1).txt) <($(NORM) target/out/$(1).txt) || true; } | cut -c1-110; fi
endef

help: ## список команд (порт і профіль: make show PORT=8090 APP_PROFILE=default)
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

build: ## зібрати застосунок (Maven у docker) у app/target/app.jar
> @echo; echo "Крок 1. Збірка застосунку. Очікувано: BUILD SUCCESS і файл app/target/app.jar"
> cd app && ../$(T)/mvn.sh -B -DskipTests package | grep -E 'Building jar|BUILD'
> @ls -l $(JAR) | awk '{print $$5 " байтів  " $$9}'

$(JAR): app/pom.xml $(shell find app/src -type f)
> @$(MAKE) --no-print-directory build

test: ## 76 автотестів (JUnit 5, MockMvc, H2): валідація, ліміти, безпечні й вразливі сторінки
> @echo; echo "Крок 2. Автотести. Очікувано: Tests run: 76, Failures: 0, Errors: 0 і BUILD SUCCESS"
> cd app && ../$(T)/mvn.sh -B test | grep -E 'Tests run:|BUILD'

up: $(JAR) ## запустити MySQL і застосунок у docker (порт PORT, профіль APP_PROFILE)
> @echo; echo "Крок 3. Стенд: MySQL 8 і застосунок на порту $(PORT), профіль $(APP_PROFILE). Очікувано: сторінка $(BASE)/register відповідає"
> @docker rm -f $(APPC) $(DBC) >/dev/null 2>&1 || true
> @docker network rm $(NET) >/dev/null 2>&1 || true
> @if ss -ltn 2>/dev/null | grep -q ":$(PORT) "; then echo "Порт $(PORT) зайнятий, запустіть з іншим портом: make show PORT=8090"; exit 1; fi
> docker network create $(NET) >/dev/null
> docker run -d --name $(DBC) --network $(NET) --network-alias db \
>   -e MYSQL_ROOT_PASSWORD=root_lab_pass -e MYSQL_DATABASE=rental -e MYSQL_USER=rental_app -e MYSQL_PASSWORD=rental_app_pass \
>   -v "$$PWD/app/sql/init.sql:/docker-entrypoint-initdb.d/01-init.sql:ro" \
>   mysql:8 --character-set-server=utf8mb4 --collation-server=utf8mb4_0900_ai_ci >/dev/null
> @n=0; until docker exec $(DBC) mysqladmin ping -h 127.0.0.1 -urental_app -prental_app_pass --silent >/dev/null 2>&1; do n=$$((n+1)); \
>   if [ $$n -gt 90 ]; then echo "MySQL не запустився"; docker logs $(DBC) 2>&1 | tail -20; exit 1; fi; sleep 2; done; echo "MySQL готовий"
> docker run -d --name $(APPC) --network $(NET) -p 127.0.0.1:$(PORT):8080 \
>   -e 'DB_URL=jdbc:mysql://db:3306/rental?useUnicode=true&characterEncoding=UTF-8' -e DB_USER=rental_app -e DB_PASSWORD=rental_app_pass \
>   -e SPRING_PROFILES_ACTIVE=$(APP_PROFILE) -v "$$PWD/$(JAR):/opt/app.jar:ro" $(IMG) java -jar /opt/app.jar >/dev/null
> @n=0; until curl -s -o /dev/null $(BASE)/register; do n=$$((n+1)); \
>   if [ $$n -gt 90 ]; then echo "Застосунок не запустився"; docker logs $(APPC) 2>&1 | tail -20; exit 1; fi; sleep 2; done; \
>   echo "Застосунок працює: $(BASE)/register"

# Стенд піднімається сам, якщо його ще немає
ready: $(JAR)
> @if [ -z "$$(docker ps -q -f name='^$(APPC)$$')" ] || ! curl -s -o /dev/null $(BASE)/register; then $(MAKE) --no-print-directory up; fi
> @mkdir -p target/out target/tmp

bypass: ready ## Атака 1: обхід клієнтської перевірки запитами curl, mass assignment
> @echo; echo "Крок 4. Атака 1: запити без браузера. Очікувано: HTTP 400 і повідомлення по кожному полю, role=ADMIN ігнорується (у БД роль CLIENT)"
> $(RESET)
> @demo/01_client_bypass.sh 2>&1 | tee target/out/01_client_bypass.txt
> $(call CMP,01_client_bypass)

sqli: ready ## Атака 2: SQL-ін'єкція до захисту (витік хешів) і після (параметризований запит)
> @echo; echo "Крок 5. Атака 2: SQL-ін'єкція. Очікувано: на /demo/vuln повертаються всі авто й хеші з users, на /demo/safe збігів немає, DROP не виконано"
> $(RESET)
> @demo/02_sqli.sh 2>&1 | tee target/out/02_sqli.txt
> $(call CMP,02_sqli)
> $(if $(filter 1,$(FRESH)),@echo "Різниця очікувана: у звіті таблиця users уже містила attacker1 після атаки 1; окремий крок починається з чистої таблиці (є лише manager). У make show різниці немає.",@:)

xss: ready ## Атака 3: XSS (вразлива сторінка проти екранування й CSP)
> @echo; echo "Крок 6. Атака 3: XSS. Очікувано: на /demo/vuln тег <script> лишається як є, на /demo/safe маємо &lt;script&gt; і заголовок Content-Security-Policy"
> $(RESET)
> @demo/03_xss.sh 2>&1 | tee target/out/03_xss.txt
> $(call CMP,03_xss)
> @echo; echo "Виконання скрипту в браузері (Playwright) тут не відтворюється, збережений протокол demo/xss-dialogs.txt:"
> @cat demo/xss-dialogs.txt

overflow: ready ## Атака 4: переповнення довжини, тіла запиту, int, неправильні типи
> @echo; echo "Крок 7. Атака 4: переповнення й неправильні типи. Очікувано: вразливо 500, безпечно 400 (довге поле) і 413 (тіло 5 МБ, також chunked)"
> $(RESET)
> @TMPDIR="$$PWD/target/tmp" demo/04_overflow_types.sh 2>&1 | tee target/out/04_overflow_types.txt
> $(call CMP,04_overflow_types)

db: ready ## запис у БД: роль CLIENT і BCrypt-хеш після атаки з role=ADMIN
> @echo; echo "Крок 8. Що лежить у БД. Очікувано: у таблиці manager (ADMIN) і attacker1 із роллю CLIENT (не ADMIN), хеші починаються з \$$2a\$$10\$$ і мають довжину 60"
> $(RESET)
> $(if $(filter 1,$(FRESH)),@curl -s -o /dev/null -w 'Реєстрація attacker1 із role=ADMIN: HTTP %{http_code}\n' -X POST $(BASE)/register --data-urlencode login=attacker1 --data-urlencode firstName=Олена --data-urlencode email=att@example.com --data-urlencode password=Secret123 --data-urlencode phone=+380501234567 --data-urlencode birthDate=1998-04-12 --data-urlencode role=ADMIN,@echo "attacker1 уже зареєстровано в кроці 4 (атака 1 із role=ADMIN)")
> @echo "\$$ docker exec $(DBC) mysql -urental_app rental -e \"SELECT id, login, first_name, LEFT(password_hash, 7) AS hash_prefix, LENGTH(password_hash) AS hash_len, role FROM users WHERE login IN ('olena_k','attacker1','manager') ORDER BY id\""
> @$(DBSH) -e "SELECT id, login, first_name, LEFT(password_hash, 7) AS hash_prefix, LENGTH(password_hash) AS hash_len, role FROM users WHERE login IN ('olena_k','attacker1','manager') ORDER BY id" 2>/dev/null

down: ## зупинити й видалити контейнери та мережу
> @r=$$(docker rm -f $(APPC) $(DBC) 2>/dev/null); if [ -n "$$r" ]; then echo "Контейнери $(APPC) і $(DBC) зупинено й видалено"; else echo "Контейнерів $(NAME) немає"; fi
> @docker network rm $(NET) >/dev/null 2>&1 || true

steps: build test up bypass sqli xss overflow db

show: ## усе по черзі: збірка, тести, стенд, чотири атаки, БД, зупинка
> @$(MAKE) --no-print-directory steps FRESH=0; rc=$$?; docker rm -f $(APPC) $(DBC) >/dev/null 2>&1; docker network rm $(NET) >/dev/null 2>&1; rm -rf target/out target/tmp; rmdir target 2>/dev/null; if [ $$rc -eq 0 ]; then echo; echo "Контейнери зупинено."; echo "Готово."; fi; exit $$rc

clean: ## видалити контейнери, мережу і тимчасові файли (target/out, target/tmp)
> @docker rm -f $(APPC) $(DBC) >/dev/null 2>&1 || true
> @docker network rm $(NET) >/dev/null 2>&1 || true
> rm -rf target/out target/tmp
> @rmdir target 2>/dev/null || true

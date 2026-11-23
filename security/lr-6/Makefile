# ЛР 6: аутентифікація за токеном (JWT на 2 хвилини, відхилені токени, журнал). Повний показ: make show
.RECIPEPREFIX := >
SHELL := /bin/bash
.SHELLFLAGS := -o pipefail -c
.PHONY: help build test up ready login rejected expiry log down clean show steps
T := ../../tools
PORT ?= 8086
NAME ?= lr6
BASE := http://localhost:$(PORT)
APPC := $(NAME)-app
JAR := app/target/app.jar
IMG := eclipse-temurin:21-jre
export BASE

help: ## список команд (порт змінюється так: make show PORT=8090)
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

build: ## зібрати застосунок (Maven у docker) у app/target/app.jar
> @echo; echo "Крок 1. Збірка застосунку. Очікувано: BUILD SUCCESS і файл app/target/app.jar"
> cd app && ../$(T)/mvn.sh -B -DskipTests package | grep -E 'Building jar|BUILD'
> @ls -l $(JAR) | awk '{print $$5 " байтів  " $$9}'

$(JAR): app/pom.xml $(shell find app/src -type f)
> @$(MAKE) --no-print-directory build

test: ## 18 автотестів (JUnit 5, MockMvc): строк 120 с, відхилені токени, журнал
> @echo; echo "Крок 2. Автотести. Очікувано: Tests run: 18, Failures: 0, Errors: 0 і BUILD SUCCESS"
> cd app && ../$(T)/mvn.sh -B test | grep -E 'Tests run:|BUILD'

up: $(JAR) ## запустити застосунок у docker на порту PORT (секрет підпису випадковий, журнал у app/logs)
> @echo; echo "Крок 3. Запуск застосунку на порту $(PORT). Очікувано: застосунок працює, форма входу на $(BASE)/"
> @docker rm -f $(APPC) >/dev/null 2>&1 || true
> @if ss -ltn 2>/dev/null | grep -q ":$(PORT) "; then echo "Порт $(PORT) зайнятий, запустіть з іншим портом: make show PORT=8090"; exit 1; fi
> @rm -rf app/logs && mkdir -p app/logs
> docker run -d --name $(APPC) -u "$$(id -u):$$(id -g)" -w /work -p 127.0.0.1:$(PORT):8080 \
>   -v "$$PWD/$(JAR):/opt/app.jar:ro" -v "$$PWD/app/logs:/work/logs" $(IMG) java -jar /opt/app.jar >/dev/null
> @n=0; until curl -s -o /dev/null $(BASE)/; do n=$$((n+1)); \
>   if [ $$n -gt 60 ]; then echo "Застосунок не запустився"; docker logs $(APPC) 2>&1 | tail -20; exit 1; fi; sleep 2; done; \
>   echo "Застосунок працює: $(BASE)/"

# Стенд піднімається сам, якщо його ще немає
ready: $(JAR)
> @if [ -z "$$(docker ps -q -f name='^$(APPC)$$')" ] || ! curl -s -o /dev/null $(BASE)/; then $(MAKE) --no-print-directory up; fi

login: ready ## вхід: 401 для хибного пароля й невідомого логіна, токен на 120 с, профіль із токеном
> @echo; echo "Крок 4. Вхід і доступ із токеном. Очікувано: однакова 401 для хибного пароля й невідомого логіна, потім 200, exp - iat = 120 с, профіль 200"
> @demo/01_login_access.sh

rejected: ready ## відхилені токени: без токена, змінений вміст, alg=none, сміття, обхід шляху
> @echo; echo "Крок 5. Токени, які сервер відхиляє. Очікувано: 401 з причинами missing, bad_signature, wrong_alg, malformed (шляхи /api;x/profile і /%61pi/profile теж missing)"
> @demo/02_rejected_tokens.sh

expiry: ready ## строк дії: профіль одразу (200) і через 121 секунду (401 expired), триває понад 2 хвилини
> @echo; echo "Крок 6. Строк дії токена. Очікувано: 200 одразу, потім чекання 121 секунда і 401 expired (крок триває понад 2 хвилини)"
> @demo/03_expiry.sh

log: login rejected ## журнал невдалих спроб app/logs/auth-failures.log без повних токенів
> @echo; echo "Крок 7. Журнал невдалих спроб. Очікувано: рядки LOGIN_FAILED і TOKEN_REJECTED, повних токенів немає (grep -c eyJ дає 0); рядок reason=expired додається після кроку expiry"
> @demo/04_log.sh; rc=$$?; \
>   if [ $$rc -eq 1 ]; then echo "grep не знайшов жодного eyJ: повних токенів у журналі немає (код 1 означає нуль збігів)"; \
>   elif [ $$rc -eq 0 ]; then echo "УВАГА: у журналі знайдено повний токен, рядків з eyJ: $$(grep -c eyJ app/logs/auth-failures.log)"; exit 1; \
>   else echo "УВАГА: журнал app/logs/auth-failures.log не вдалося прочитати (код $$rc)"; exit 1; fi

down: ## зупинити й видалити контейнер
> @r=$$(docker rm -f $(APPC) 2>/dev/null); if [ -n "$$r" ]; then echo "Контейнер $(APPC) зупинено й видалено"; else echo "Контейнера $(APPC) немає"; fi

steps: build test up login rejected expiry log

show: ## усе по черзі: збірка, тести, запуск, вхід, відхилені токени, строк дії, журнал, зупинка (понад 3 хвилини)
> @$(MAKE) --no-print-directory steps; rc=$$?; docker rm -f $(APPC) >/dev/null 2>&1; if [ $$rc -eq 0 ]; then echo; echo "Контейнер зупинено."; echo "Готово."; fi; exit $$rc

clean: ## видалити контейнер і журнал app/logs
> @docker rm -f $(APPC) >/dev/null 2>&1 || true
> rm -rf app/logs

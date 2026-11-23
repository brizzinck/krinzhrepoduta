# СР 2: Camera API з H2 і Spring Data JPA. Повний показ: make show
.RECIPEPREFIX := >
SHELL := /bin/bash
.SHELLFLAGS := -o pipefail -c
.PHONY: help build up info import saved delete reimport restart consoles down clean show
PORT ?= 8081
NAME ?= sr2-app
B := http://localhost:$(PORT)/api/v1/camera
IMG := maven:3.9-eclipse-temurin-21
JAR := target/demo-0.0.1-SNAPSHOT.jar
# мережа хоста потрібна для H2 Console (вона приймає лише локальні з'єднання);
# база лежить у demo/target/data (не в git), порт задає змінна PORT
RUN := docker run -d --name $(NAME) --network host -u "$$(id -u):$$(id -g)" -e LANG=C.UTF-8 -v "$$PWD/demo":/w -w /w --entrypoint java $(IMG) -jar $(JAR) --server.port=$(PORT) --spring.datasource.url=jdbc:h2:file:./target/data/cameradb
WAIT := until curl -sf -o /dev/null http://localhost:$(PORT)/v3/api-docs; do docker inspect -f '{{.State.Running}}' $(NAME) | grep -q true || { echo "Застосунок не запустився, можливо порт $(PORT) зайнятий"; docker logs --tail 5 $(NAME); exit 1; }; sleep 1; done; docker inspect -f '{{.State.Running}}' $(NAME) | grep -q true || { echo "Застосунок не запустився, можливо порт $(PORT) зайнятий"; exit 1; }
CODE := curl -s -o /dev/null -w 'HTTP %{http_code}\n'

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

build: down ## зібрати jar (Maven у docker)
> @echo; echo "Крок 1. Збірка проєкту. Очікувано: BUILD SUCCESS"
> @cd demo && ../../../tools/mvn.sh clean package | grep -E 'Compiling|Building jar|BUILD'

demo/$(JAR):
> @echo; echo "Jar ще немає, збираємо його (Maven у docker)"
> @cd demo && ../../../tools/mvn.sh package | grep -E 'Compiling|Building jar|BUILD'

up: demo/$(JAR) ## запустити застосунок у docker з чистою базою H2 (порт PORT); сам збере jar
> @docker inspect -f '{{.State.Running}}' $(NAME) 2>/dev/null | grep -q true || { docker rm -f $(NAME) >/dev/null 2>&1; rm -rf demo/target/data; echo; echo "Запуск застосунку $(NAME) на порту $(PORT), база demo/target/data"; $(RUN) >/dev/null && $(WAIT); }

info: up ## старі ендпоїнти СР 1: GET /info і POST /snapshot
> @echo; echo "Крок 2. Старі ендпоїнти. Очікувано: HTTP 200 для /info, HTTP 200 і image/jpeg для /snapshot"
> @echo "> curl -i $(B)/info"
> @curl -s -i $(B)/info | sed -n '1p;$$p' | cut -c1-160
> @echo "> curl -X POST $(B)/snapshot -H 'Accept: image/jpeg'"
> @curl -s -X POST $(B)/snapshot -H 'Accept: image/jpeg' -D - -o /dev/null | grep -E '^HTTP|^Content-Type|^Content-Length' | tr -d '\r'

import: up ## POST /import: список камер із DummyJSON у H2
> @echo; echo "Крок 3. Імпорт камер. Очікувано: HTTP 200 і чотири камери з id 1-4 (location розкладено на room і floor)"
> @echo "> curl -X POST $(B)/import"
> @curl -s -w '\nHTTP %{http_code}\n' -X POST $(B)/import | cut -c1-260

saved: import ## GET /saved і GET /saved/1 читають лише з H2
> @echo; echo "Крок 4. Читання з H2. Очікувано: у списку id 1-4, GET /saved/1 дає HTTP 200"
> @echo "> curl $(B)/saved   (лише id)"
> @curl -s $(B)/saved | grep -o '"id":[0-9]*' | tr '\n' ' '; echo
> @echo "> curl -i $(B)/saved/1"
> @curl -s -w '\nHTTP %{http_code}\n' $(B)/saved/1

delete: import ## DELETE /saved/1, потім GET дає 404
> @echo; echo "Крок 5. Видалення. Очікувано: DELETE HTTP 204, наступний GET /saved/1 HTTP 404"
> @echo "> curl -X DELETE $(B)/saved/1"
> @$(CODE) -X DELETE $(B)/saved/1
> @echo "> curl $(B)/saved/1"
> @$(CODE) $(B)/saved/1

reimport: delete ## повторний імпорт повертає камеру 1 без дублів
> @echo; echo "Крок 6. Повторний імпорт. Очікувано: HTTP 200, у базі знову id 1-4 по одному разу"
> @echo "> curl -X POST $(B)/import"
> @$(CODE) -X POST $(B)/import
> @echo "> curl $(B)/saved   (лише id)"
> @curl -s $(B)/saved | grep -o '"id":[0-9]*' | tr '\n' ' '; echo
> @echo "Журнал Hibernate для другого імпорту: 4 select за id і 1 insert (лише камера 1)"
> @docker logs $(NAME) 2>&1 | awk '/Importing 4 cameras/ {n++; s=0; i=0} n==2 && /Hibernate: select.*where ce1_0.id=/ {s++} n==2 && /Hibernate: insert/ {i++} END {print "select: " s ", insert: " i}'

restart: reimport ## перезапуск: дані лишились у файлі H2
> @echo; echo "Крок 7. Перезапуск застосунку без імпорту. Очікувано: GET /saved знову дає id 1-4 (база у файлі)"
> @docker stop $(NAME) >/dev/null && docker rm $(NAME) >/dev/null
> @$(RUN) >/dev/null && $(WAIT)
> @echo "> curl $(B)/saved   (лише id)"
> @curl -s $(B)/saved | grep -o '"id":[0-9]*' | tr '\n' ' '; echo

consoles: up ## Swagger UI і H2 Console відповідають
> @echo; echo "Крок 8. Swagger UI і H2 Console. Очікувано: HTTP 200 для обох"
> @echo "> curl http://localhost:$(PORT)/swagger-ui/index.html"
> @$(CODE) http://localhost:$(PORT)/swagger-ui/index.html
> @echo "> curl http://localhost:$(PORT)/h2-console/"
> @$(CODE) http://localhost:$(PORT)/h2-console/
> @echo "У браузері: Swagger UI за цією адресою, H2 Console: jdbc:h2:file:./target/data/cameradb, користувач sa, порожній пароль (поки застосунок працює)."

down: ## зупинити й видалити контейнер застосунку
> @docker stop $(NAME) >/dev/null 2>&1; docker rm $(NAME) >/dev/null 2>&1; true

clean: down ## прибрати контейнер і demo/target
> @rm -rf demo/target

show: build info saved delete reimport restart consoles ## усе по черзі
> @docker stop $(NAME) >/dev/null 2>&1; docker rm $(NAME) >/dev/null 2>&1; true
> @echo; echo "Готово."

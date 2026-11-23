# СР 1: мікросервіс камери (Spring Boot 3.3.5, OpenFeign, Swagger UI). Повний показ: make show
.RECIPEPREFIX := >
.PHONY: help build up ready info snapshot notjson logs down stage1 steps show clean
T := ../../tools
IMG := maven:3.9-eclipse-temurin-21
PORT ?= 8081
NAME ?= sr1-app
DIR ?= demo
BASE := http://localhost:$(PORT)
API := $(BASE)/api/v1/camera
UID_GID = $$(id -u):$$(id -g)

help: ## список команд (порт змінюється так: make show PORT=8082)
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

build: ## mvn clean package у demo (docker)
> @echo; echo "Крок 1. Збірка. Очікувано: Compiling 6 source files, BUILD SUCCESS"
> cd demo && ../../../tools/mvn.sh clean package | grep -E 'Compiling|Building jar|BUILD'

demo/target/demo-0.0.1-SNAPSHOT.jar:
> @$(MAKE) --no-print-directory build

up: demo/target/demo-0.0.1-SNAPSHOT.jar ## запустити застосунок у docker на порту PORT (потрібен інтернет: DummyJSON)
> @echo; echo "Крок 2. Запуск застосунку на порту $(PORT). Очікувано: застосунок працює"
> @docker rm -f $(NAME) >/dev/null 2>&1 || true
> @if ss -ltn 2>/dev/null | grep -q ":$(PORT) "; then echo "Порт $(PORT) зайнятий, запустіть з іншим портом: make show PORT=8082"; exit 1; fi
> docker run -d --name $(NAME) -p $(PORT):8080 -u "$(UID_GID)" -e LANG=C.UTF-8 -v "$$PWD/$(DIR):/w" -w /w --entrypoint java $(IMG) -jar target/demo-0.0.1-SNAPSHOT.jar >/dev/null
> @n=0; until curl -s -o /dev/null $(BASE)/v3/api-docs; do n=$$((n+1)); \
>   if [ $$n -gt 90 ]; then echo "Застосунок не піднявся"; docker logs $(NAME) 2>&1 | tail -20; exit 1; fi; sleep 1; done; \
>   echo "Застосунок працює, Swagger UI: $(BASE)/swagger-ui/index.html"

ready: demo/target/demo-0.0.1-SNAPSHOT.jar
> @curl -s -o /dev/null $(BASE)/v3/api-docs || $(MAKE) --no-print-directory up

info: ready ## GET /info: 200 і JSON із полями камери
> @echo; echo "Крок 3. GET /info. Очікувано: 200 і поля id, device_type, status, features, location"
> curl -s -i $(API)/info | tr -d '\r' | grep -E '^HTTP|^Content-Type|^\{'

snapshot: ready ## POST /snapshot з Accept: image/jpeg: 200, image/jpeg, файл JPEG
> @echo; echo "Крок 4. POST /snapshot (JPEG). Очікувано: 200, image/jpeg, JPEG 900x600, хеш збігається з demo/results/snapshot.jpg"
> curl -s -X POST $(API)/snapshot -H "Accept: image/jpeg" -D demo/target/snapshot-headers.txt -o demo/target/snapshot.jpg
> @tr -d '\r' < demo/target/snapshot-headers.txt | grep -E '^HTTP|^Content-'
> file -b demo/target/snapshot.jpg | sed 's/,.*, \([0-9]*x[0-9]*\),.*/, \1/'
> sha256sum demo/target/snapshot.jpg demo/results/snapshot.jpg

notjson: ready ## POST /snapshot з Accept: application/json: 406
> @echo; echo "Крок 5. Той самий запит із Accept: application/json. Очікувано: 406"
> curl -s -o /dev/null -w 'HTTP %{http_code}\n' -X POST $(API)/snapshot -H "Accept: application/json"

logs: info snapshot ## логи CameraService (спершу робить запити, щоб було що показати)
> @echo; echo "Крок 6. Логи сервісу. Очікувано: Getting camera info, Camera info received, Capturing snapshot, Snapshot received"
> docker logs $(NAME) 2>&1 | grep CameraService | sed 's/.*CameraService *: //' | cut -c1-100

down: ## зупинити й видалити контейнер
> @if docker rm -f $(NAME) 2>/dev/null | grep -q .; then echo "Контейнер $(NAME) зупинено й видалено"; else echo "Контейнера $(NAME) немає"; fi

stage1: demo/target/demo-0.0.1-SNAPSHOT.jar ## етап 1 (JSON із Base64) на окремій копії проєкту в demo/target/stage1
> @echo; echo "Крок 7. Етап 1: POST /snapshot повертає JSON. Очікувано: application/json, 142632 символи, 106972 байти, початок ffd8ff"
> rm -rf demo/target/stage1 && mkdir -p demo/target/stage1
> cp -r demo/pom.xml demo/src demo/check_image.sh demo/target/stage1/
> cp demo/stages/CameraController.stage1.java demo/target/stage1/src/main/java/com/example/demo/controller/CameraController.java
> cd demo/target/stage1 && ../../../../../tools/mvn.sh clean package | grep BUILD
> @$(MAKE) --no-print-directory up DIR=demo/target/stage1 >/dev/null
> curl -s -i -X POST $(API)/snapshot | tr -d '\r' | head -c 200 | grep -E '^HTTP|^Content-Type'
> cd demo/target/stage1 && sh check_image.sh $(API)/snapshot
> @$(MAKE) --no-print-directory down >/dev/null

steps: build up info snapshot notjson logs down stage1

show: ## усе по черзі: збірка, запуск, запити, зупинка (контейнер і порт звільняються в кінці)
> @$(MAKE) --no-print-directory steps; rc=$$?; docker rm -f $(NAME) >/dev/null 2>&1; if [ $$rc -eq 0 ]; then echo; echo "Готово."; fi; exit $$rc

clean: ## видалити контейнер і demo/target
> @docker rm -f $(NAME) >/dev/null 2>&1 || true
> rm -rf demo/target

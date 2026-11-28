# ПР 9: гонитва потоків і взаємне блокування (варіант 2). Повний показ: make show
.RECIPEPREFIX := >
SHELL := /bin/bash
.SHELLFLAGS := -o pipefail -c
.PHONY: help build race synced deadlock fixed clean show
T := ../../tools
P := ua.duikt.lavrishko.pr9
IMG := maven:3.9-eclipse-temurin-21
# ім'я контейнера, у якому зависає програма
NAME ?= pr9-deadlock
RUNJ := $(T)/java.sh -cp target/classes $(P)

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

build: ## скомпілювати класи (javac у docker, без бібліотек)
> @$(T)/javac.sh -encoding UTF-8 -d target/classes src/main/java/ua/duikt/lavrishko/pr9/*.java

race: build ## дослід 1: без synchronized, три запуски
> @echo; echo "Крок 1. Лічильник без synchronized. Очікувано: менше 400000, щоразу інше число"
> @for i in 1 2 3; do echo "> java -cp target/classes $(P).Exp1Race plain"; $(RUNJ).Exp1Race plain; done

synced: build ## дослід 1: з synchronized, три запуски
> @echo; echo "Крок 2. Метод synchronized. Очікувано: тричі 400000 / 400000"
> @for i in 1 2 3; do echo "> java -cp target/classes $(P).Exp1Race synced"; $(RUNJ).Exp1Race synced; done

deadlock: build ## дослід 2: зависання і jstack
# весь рецепт в одному shell, trap видаляє контейнер навіть після помилки чи Ctrl+C
> @trap 'docker rm -f $(NAME) >/dev/null 2>&1' EXIT INT TERM; \
> docker rm -f $(NAME) >/dev/null 2>&1 || true; \
> echo; echo "Крок 3. Наївне перенесення між дошками. Очікувано: два рядки тримає/чекає, без рядка про завершення, контейнер Up, у дампі Found one Java-level deadlock"; \
> echo "> docker run -d --name $(NAME) ... java -cp target/classes $(P).Exp2Deadlock naive"; \
> docker run -d --name $(NAME) -e LANG=C.UTF-8 -v "$$PWD":/w:ro -w /w --entrypoint java $(IMG) -cp target/classes $(P).Exp2Deadlock naive >/dev/null; \
> sleep 2; \
> echo "> docker logs $(NAME)"; \
> docker logs $(NAME); \
> echo "> docker ps --filter name=$(NAME)   (Up означає, що програма зависла)"; \
> docker ps --filter name=$(NAME) --format '{{.Status}}'; \
> echo "> docker exec $(NAME) jps -l"; \
> docker exec $(NAME) jps -l; \
> echo "> docker exec $(NAME) jstack 1   (фрагмент дампу)"; \
> docker exec $(NAME) jstack 1 | sed -n '/^Found one Java-level deadlock/,/^Found 1 deadlock/p'

fixed: build ## дослід 2: монітори за зростанням id, програма завершується
> @echo; echo "Крок 4. Порядок за id. Очікувано: Програма завершилась"
> @echo "> java -cp target/classes $(P).Exp2Deadlock ordered"
> @$(RUNJ).Exp2Deadlock ordered

clean: ## прибрати target/
> @rm -rf target

show: race synced deadlock fixed clean ## усе по черзі
> @echo; echo "Готово."

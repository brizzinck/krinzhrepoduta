# ЛР 1: модель загроз «Прокат автомобілів» (OWASP Threat Dragon). Повний показ: make show
.RECIPEPREFIX := >
.PHONY: help check rebuild tables figures up down open clean show
PORT ?= 3000
NAME ?= td_lr1
URL := http://localhost:$(PORT)
# Python у docker, на хості нічого не встановлюється; -I вимикає сторонні модулі
PY := docker run --rm -u "$$(id -u):$$(id -g)" -v "$$PWD":/w -w /w python:3.12-slim python -I -B

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

check: ## модель з Threat Dragon: склад діаграми й підсумки загроз проти звіту
> @echo; echo "Крок 1. Перевірка збереженої моделі model/rental-threat-model.json. Очікувано: 5 EI, 6 P, 4 DS, 6 меж, 18 потоків; 29 загроз у 55 записах; 11 High; рядок «Усі числа збігаються зі звітом.»"
> @$(PY) threat-dragon/check_model.py model/rental-threat-model.json

rebuild: ## модель з вихідних даних: build_model.py і порівняння з файлом у репозиторії
> @echo; echo "Крок 2. Генерація JSON для імпорту в Threat Dragon з rental_model.py. Очікувано: готово: ... 55 записів загроз; diff порожній"
> @mkdir -p target
> @$(PY) threat-dragon/build_model.py target/rental-threat-model.input.json
> @diff target/rental-threat-model.input.json threat-dragon/rental-threat-model.input.json && echo "Збігається з threat-dragon/rental-threat-model.input.json."

tables: ## таблиці threats.md і architecture.md з JSON-моделі: td2md.py і порівняння
> @echo; echo "Крок 3. Таблиці звіту з JSON, збереженого в Threat Dragon. Очікувано: diff порожній для threats.md і architecture.md"
> @mkdir -p target/model
> @$(PY) threat-dragon/td2md.py model/rental-threat-model.json target/model/
> @diff target/model/threats.md model/threats.md && echo "model/threats.md збігається."
> @diff target/model/architecture.md model/architecture.md && echo "model/architecture.md збігається."
> @echo "Рядки реєстру T01-T29 і FP1-FP4: $$(grep -cE '^\| (T|FP)[0-9]+ ' target/model/threats.md)"

figures: ## рисунки звіту й знімки інтерфейсу Threat Dragon з розмірами
> @echo; echo "Крок 4. Знімки з живого Threat Dragon, використані в звіті. Очікувано: три рисунки звіту й решта знімків у img/"
> @n=0; for f in $$(grep -o '](img/[^)]*)' report.md | sed 's/](//; s/)//'); do n=$$((n+1)); echo "Рисунок $$n: $$f ($$(wc -c < $$f) байт)"; done
> @echo "Усі знімки в img/:"; ls -1 img | sed 's/^/  img\//'

up: ## запустити Threat Dragon v2.2.0 в docker на $(URL)
> @echo; echo "Крок 5. Запуск Threat Dragon в docker. Очікувано: відповідь HTTP 200 на $(URL)"
> @TD_PORT=$(PORT) TD_NAME=$(NAME) threat-dragon/run.sh start >/dev/null
> @for i in $$(seq 1 30); do code=$$(curl -s -o /dev/null -w '%{http_code}' $(URL)/ 2>/dev/null || true); [ "$$code" = 200 ] && break; sleep 1; done; \
>   echo "HTTP $$code, контейнер $(NAME), адреса $(URL)"; [ "$$code" = 200 ] || { echo "Threat Dragon не відповів: перевір, що Docker запущений і порт $(PORT) вільний."; exit 1; }
> @echo "Вхід: Login to Local Session. Модель: Open Existing Model, вставити вміст model/rental-threat-model.json."

down: ## зупинити й видалити контейнер Threat Dragon
> @docker rm -f $(NAME) >/dev/null 2>&1 || true
> @echo "Контейнер $(NAME) видалено."

open: ## відкрити звіт у PDF і схему DFD (потрібна програма перегляду на комп'ютері)
> @setsid -f xdg-open report.pdf >/dev/null 2>&1 && echo "Відкрито: report.pdf" || echo "Відкрий вручну: report.pdf"
> @setsid -f xdg-open img/dfd.png >/dev/null 2>&1 && echo "Відкрито: img/dfd.png" || echo "Відкрий вручну: img/dfd.png"

clean: down ## видалити тимчасові файли (target/) і контейнер
> @rm -rf target

show: check rebuild tables figures up down clean ## усе по черзі, у кінці прибирає за собою
> @echo; echo "Готово."

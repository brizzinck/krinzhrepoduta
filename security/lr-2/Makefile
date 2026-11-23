# ЛР 2: вимоги безпеки ASVS 5.0.0 до елементів «Прокат автомобілів». Повний показ: make show
.RECIPEPREFIX := >
.PHONY: help numbers rule coverage report element open clean show
# елемент або потік для `make element`, наприклад E=P-3, DS-1, EI-4, F9
E ?= P-3
# Python у docker, на хості нічого не встановлюється; монтується security/, бо скрипт читає ще й ЛР 1
PY := docker run --rm -u "$$(id -u):$$(id -g)" -v "$$PWD/..":/s -w /s/lr-2 python:3.12-slim python -I scripts/check_requirements.py

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

numbers: ## числа з requirements.md: 208 рядків, 133 ID ASVS, 101 обов'язкова
> @echo; echo "Крок 1. Підрахунок вимог у requirements.md. Очікувано: 208 рядків, 133 різних ID, обов'язкових 101 / етап 2 95 / етап 3 12, L1 не застосовано 14 з 70"
> @$(PY) numbers

rule: ## правило поділу: L1 обов'язкові, L3 ні, L2 лише проти високих загроз
> @echo; echo "Крок 2. Перевірка правила поділу на обов'язкові й рекомендовані. Очікувано: порожні списки винятків"
> @$(PY) rule

coverage: ## кожна загроза T01-T29 з ЛР 1 має вимогу, кожна висока має обов'язкову
> @echo; echo "Крок 3. Покриття загроз вимогами (реєстр береться з ../lr-1/model/threats.md). Очікувано: 29 загроз, 11 високих, порожні списки пропусків"
> @$(PY) coverage

report: ## таблиця розділу 4 звіту проти requirements.md
> @echo; echo "Крок 4. Числа таблиці звіту порівнюються з підрахунком за requirements.md. Очікувано: рядок «Усього: 101 / 95 / 12 / 208» без позначок розбіжності"
> @$(PY) report

element: ## вимоги одного елемента з requirements.md (за замовчуванням E=P-3)
> @echo; echo "Вимоги елемента $(E) з requirements.md. Очікувано: таблиця з ID ASVS, рівнем, типом і загрозами"
> @awk -v e="$(E)" 'BEGIN{p=0;f=0} /^##/{p=0} /^### /{h=$$0; sub(/^### /,"",h); sub(/ \(.*$$/,"",h); n=split(h,w,/[ ,]+/); for(i=1;i<=n;i++) if(w[i]==e){p=1;f=1}} p{print} END{if(!f) print "Розділу для " e " немає: список заголовків дивись командою grep \"^###\" requirements.md"}' requirements.md | cut -c1-200

open: ## відкрити звіт у PDF і схему архітектури (потрібна програма перегляду)
> @setsid -f xdg-open report.pdf >/dev/null 2>&1 && echo "Відкрито: report.pdf" || echo "Відкрий вручну: report.pdf"
> @setsid -f xdg-open img/dfd.png >/dev/null 2>&1 && echo "Відкрито: img/dfd.png" || echo "Відкрий вручну: img/dfd.png"

clean: ## нічого видаляти: скрипт лише читає файли
> @echo "Тимчасових файлів немає."

show: numbers rule coverage report ## усе по черзі
> @echo; echo "Готово."

# ЛР 4-5: тестування безпеки на PortSwigger Web Security Academy (Burp). Повний показ: make show
.RECIPEPREFIX := >
.PHONY: help labs figures enum access jwt oauth open clean show
# Python у docker, на хості нічого не встановлюється; -I вимикає сторонні модулі
PY := docker run --rm -u "$$(id -u):$$(id -g)" -v "$$PWD":/w -w /w python:3.12-slim python -I scripts/models.py

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

labs: ## шість вправ зі звіту й де їх відтворити
> @echo; echo "Крок 1. Вправи зі звіту. Очікувано: шість вправ, чотири класи вразливостей"
> @echo "make стенди не запускає: вони відкриваються лише в браузері в акаунті Web Security Academy і живуть обмежений час."
> @echo "Доказ проходження: знімки Solved нижче (make figures); повторити можна на https://portswigger.net/web-security (All labs)."
> @grep -E '^## 3\.[0-9]\.' report.md | sed 's/^## /  /'

figures: ## рисунки звіту: номер, файл, розмір, оригінал без обрізання
> @echo; echo "Крок 2. Знімки зі звіту. Очікувано: 8 рисунків (6 станів Solved і 2 кадри Burp), усі файли існують"
> @n=0; for f in $$(grep -o '](screenshots/[^)]*)' report.md | sed 's/](//; s/)//'); do n=$$((n+1)); \
>   [ -f "$$f" ] || { echo "Немає файлу $$f"; exit 1; }; echo "Рисунок $$n: $$f ($$(wc -c < $$f) байт)"; done
> @echo "Кадри Burp: Intruder (вправа 1, рис. 1) і Repeater (вправа 4, рис. 5); повні оригінали лежать поруч без -crop:"
> @ls -1 screenshots | grep -v -- '-crop' | sed 's/^/  screenshots\//'

enum: ## вправа 1: різні відповіді форми входу, локальна модель
> @echo; echo "Крок 3. Вправа 1, модель механізму (не стенд). Очікувано: у вразливій версії одне імʼя має іншу довжину відповіді і підбирається пароль, у виправленій довжини однакові"
> @$(PY) enum

access: ## вправи 2 і 3: прихована адреса адмінки та масове присвоєння roleid, локальна модель
> @echo; echo "Крок 4. Вправи 2 і 3, модель механізму (не стенд). Очікувано: вразливий сервер дає 200 і roleid 2, виправлений дає 403 і лишає roleid 1"
> @$(PY) access

jwt: ## вправи 4 і 5: підміна sub у JWT і alg=none, локальна модель із розбором токена
> @echo; echo "Крок 5. Вправи 4 і 5, модель механізму (не стенд): токен створено, розібрано й підроблено локально, у лабораторіях алгоритм був RS256, у моделі HS256. Очікувано: вразливі сервери бачать administrator, сервер із перевіркою підпису відхиляє обидва токени"
> @$(PY) jwt

oauth: ## вправа 6: довіра до email у тілі запиту /authenticate, локальна модель
> @echo; echo "Крок 6. Вправа 6, модель механізму (не стенд). Очікувано: вразливий клієнт створює сесію для carlos, виправлений лишає wiener"
> @$(PY) oauth

open: ## відкрити звіт у PDF і кадр Burp Repeater (потрібна програма перегляду)
> @setsid -f xdg-open report.pdf >/dev/null 2>&1 && echo "Відкрито: report.pdf" || echo "Відкрий вручну: report.pdf"
> @setsid -f xdg-open screenshots/lab4-burp-repeater.png >/dev/null 2>&1 && echo "Відкрито: screenshots/lab4-burp-repeater.png" || echo "Відкрий вручну: screenshots/lab4-burp-repeater.png"

clean: ## нічого видаляти: цілі лише читають файли й друкують
> @echo "Тимчасових файлів немає."

show: labs figures enum access jwt oauth ## усе по черзі
> @echo; echo "Готово."

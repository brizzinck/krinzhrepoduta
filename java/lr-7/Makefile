# ЛР 7: звіт за набором даних засобами Stream API. Повний показ: make show
.RECIPEPREFIX := >
.PHONY: help build found missing usage check show
T := ../../tools
R := $(T)/java.sh -jar target/report.jar

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

build: ## зібрати target/report.jar Maven у docker
> @$(T)/mvn.sh -q clean package

found: build ## звіт для наявного ключа SHOP-101
> @echo; echo "Крок 1. Ключ є. Очікувано: 40 рядків, 36 унікальних, підсумки за статусами, топ-5, Пошук SHOP-101: ... 8 SP"
> $(R) data/v2_issues.txt SHOP-101

missing: build ## звіт для відсутнього ключа SHOP-000
> @echo; echo "Крок 2. Ключа немає. Очікувано: ті самі підсумки і топ-5, Пошук SHOP-000: не знайдено, без винятку"
> $(R) data/v2_issues.txt SHOP-000

usage: build ## запуск без ключа
> @echo; echo "Крок 3. Без другого аргументу. Очікувано: рядок Використання: ... і код 2"
> @$(R) data/v2_issues.txt || echo "(код завершення: $$?)"

check: ## перевірка підсумків без Java: sort -u і awk
> @echo; echo "Крок 4. Незалежна перевірка. Очікувано: 40 рядків, 36 унікальних, DONE 11 77, IN_PROGRESS 9 36, REVIEW 8 33, TODO 8 35, шість задач із 13 SP"
> @tail -n +2 data/v2_issues.txt | wc -l
> @tail -n +2 data/v2_issues.txt | tr -d '\r' | sort -u | wc -l
> @tail -n +2 data/v2_issues.txt | tr -d '\r' | sort -u | awk -F';' '{c[$$2]++; s[$$2]+=$$4} END {for (k in c) print k, c[k], s[k]}' | sort
> @tail -n +2 data/v2_issues.txt | tr -d '\r' | sort -u | awk -F';' '$$4 == 13 {print $$1}' | sort

show: found missing usage check ## усе по черзі
> @echo; echo "Готово."

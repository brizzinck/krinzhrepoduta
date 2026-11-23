# ЛР 6: узагальнене сховище записів (Generics, Collections). Повний показ: make show
.RECIPEPREFIX := >
.PHONY: help build run casts check show
T := ../../tools
MAIN := ua.duikt.lavrishko.lr6.Main

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

build: ## зібрати проєкт Maven у docker (target/)
> @$(T)/mvn.sh -q clean package

run: build ## обидва файли в одному сховищі: пошук, повтори, сортування, захист
> @echo; echo "Крок 1. Те саме сховище для задач і учасників. Очікувано: 40 рядків, 37 записів, 3 повтори, пошук, сортування, UnsupportedOperationException"
> $(T)/java.sh -cp target/classes $(MAIN) data/v2_issues.txt data/v2_members.txt

casts: ## у клієнтському коді немає приведень типу
> @echo; echo "Крок 2. Приведень (Issue) або (Member) у коді немає. Очікувано: порожній вивід і код 1 від grep"
> @grep -rnE '\((Issue|Member)\)' src; echo "(код завершення grep: $$?)"

check: ## незалежна перевірка 40 рядків і 37 ключів
> @echo; echo "Крок 3. Незалежна перевірка консольними утилітами. Очікувано: 40, 37, 40, 37"
> @for f in v2_issues v2_members; do echo "$$f.txt:"; tail -n +2 data/$$f.txt | wc -l; tail -n +2 data/$$f.txt | cut -d';' -f1 | sort -u | wc -l; done

show: run casts check ## усе по черзі
> @echo; echo "Готово."

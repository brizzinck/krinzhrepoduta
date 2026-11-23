# ПР 7: лямбда-вирази, Stream API, Optional. Повний показ: make show
.RECIPEPREFIX := >
.PHONY: help build sort errors classes sum peek optional get show
T := ../../tools
P := ua.duikt.lavrishko.pr7
J := $(T)/java.sh -cp target/classes $(P)

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

build: ## зібрати проєкт Maven у docker (target/)
> @$(T)/mvn.sh -q clean package

sort: build ## три способи сортування дають однаковий порядок
> @echo; echo "Крок 1. Анонімний клас, лямбда, comparingInt. Очікувано: три однакові списки, Порядок збігається: true"
> @$(J).Exp1Sort

errors: build ## calls++ у лямбді не компілюється
> @echo; echo "Крок 2. Зміна локальної змінної в лямбді. Очікувано: local variables referenced from a lambda expression must be final or effectively final"
> @$(T)/javac.sh -encoding UTF-8 -cp target/classes -d /tmp/out compile-errors/E1_calls/*.java || true

classes: build ## .class-файли: для лямбди окремого файла немає
> @echo; echo "Крок 3. Файли класів дослідів. Очікувано: Exp1Sort\$$1.class (анонімний клас), для лямбди окремого файла немає"
> @ls target/classes/ua/duikt/lavrishko/pr7/

sum: build ## цикл і потік дають ту саму суму
> @echo; echo "Крок 4. Цикл проти потоку. Очікувано: цикл: 39, потік: 39"
> @$(J).Exp2Sum

peek: build ## peek з термінальною операцією і без неї
> @echo; echo "Крок 5. peek із sum(). Очікувано: п'ять рядків peek і сума: 39"
> @$(J).Exp2Peek terminal
> @echo; echo "Крок 6. Конвеєр без термінальної операції. Очікувано: peek нічого не друкує"
> @$(J).Exp2Peek lazy

optional: build ## findFirst, Optional, map і orElse
> @echo; echo "Крок 7. Optional. Очікувано: Optional.empty і не знайдено; Optional[SHOP-101 ...] і SHOP-101"
> @$(J).Exp3Optional

get: build ## get() на порожньому Optional
> @echo; echo "Крок 8. get() без перевірки. Очікувано: NoSuchElementException: No value present"
> @$(J).Exp3Get || echo "(код завершення: $$?)"

show: sort errors classes sum peek optional get ## усе по черзі
> @echo; echo "Готово."

# ПР 6: Generics, колекції, equals і hashCode. Повний показ: make show
.RECIPEPREFIX := >
.PHONY: help build errors cast total timing cme iterator equality show
T := ../../tools
P := ua.duikt.lavrishko.pr6
J := $(T)/java.sh -cp target/classes $(P)

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

build: ## зібрати проєкт Maven у docker (target/)
> @$(T)/mvn.sh -q clean package

errors: build ## хибні фрагменти: String у Box<Issue> і List<Integer> у List<Number>
> @echo; echo "Крок 1. Помилки компіляції. Очікувано: String cannot be converted to Issue; List<Integer> cannot be converted to List<Number>"
> @for d in compile-errors/E*/; do echo "javac $$d"; $(T)/javac.sh -encoding UTF-8 -cp target/classes -d /tmp/out $$d*.java || true; done

cast: build ## ObjectBox: помилка лише під час виконання
> @echo; echo "Крок 2. Object замість T. Очікувано: компіляція пройшла, потім ClassCastException"
> @$(J).Exp1Cast || echo "(код завершення: $$?)"

total: build ## List<? extends Number> приймає List<Integer>
> @echo; echo "Крок 3. List<? extends Number>. Очікувано: 26.0 і 26.5"
> @$(J).Exp1Total

timing: build ## час 1000 вставок і 1000 contains на 100 000 елементів, 5 запусків
> @echo; echo "Крок 4. Час колекцій, мс. Очікувано: вставка в LinkedList сотні мс, в ArrayList одиниці мс, HashSet менше 1 мс"
> @$(J).Exp2Timing

cme: build ## видалення в for-each
> @echo; echo "Крок 5. Видалення в for-each. Очікувано: ConcurrentModificationException"
> @$(J).Exp2Cme || echo "(код завершення: $$?)"

iterator: build ## видалення через Iterator
> @echo; echo "Крок 6. Iterator.remove(). Очікувано: було 8, стало 5"
> @$(J).Exp2Iterator

equality: build ## HashSet у трьох станах класу і зміна ключа
> @echo; echo "Крок 7. equals і hashCode у HashSet. Очікувано: size 2/2/1, contains false/false/true; після setKey обидва contains false"
> @$(J).Exp3Equality

show: errors cast total timing cme iterator equality ## усе по черзі
> @echo; echo "Готово."

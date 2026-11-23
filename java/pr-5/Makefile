# ПР 5: перевірювані й неперевірювані винятки, власний виняток, файли й кодування. Повний показ: make show
.RECIPEPREFIX := >
.PHONY: help build checked unchecked order finally cause files clean show
T := ../../tools
P := ua.duikt.lavrishko.pr5
JC := $(T)/javac.sh -encoding UTF-8 -d target/ce -cp target/classes
J := $(T)/java.sh -cp target/classes $(P).Main

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

target/classes/$(subst .,/,$(P))/Main.class: pom.xml $(shell find src -name '*.java')
> $(T)/mvn.sh -q package

build: target/classes/$(subst .,/,$(P))/Main.class ## зібрати проєкт (Maven у docker) у target/

checked: build ## Дослід 1: перевірюваний виняток, try-catch і throws
> @echo; echo "Крок 1. FileReader без обробки. Очікувано: unreported exception FileNotFoundException; must be caught or declared to be thrown"
> @mkdir -p target/ce target/out
> $(JC) compile-errors/E1_unreported/*.java; echo "(код завершення: $$?)"
> @echo; echo "Крок 2. Виправлені версії (try-catch і throws). Очікувано: компілюються, код 0"
> $(JC) compile-errors/fixed/*.java; echo "(код завершення: $$?)"
> @echo; echo "Крок 3. Обробка в try-catch. Очікувано: Файл не знайдено: nofile.txt, код 0"
> $(J) exp1 catch nofile.txt; echo "(код завершення: $$?)"
> @echo; echo "Крок 4. Передача через throws. Очікувано: виняток дійшов до main, код 1"
> $(J) exp1 throws nofile.txt > target/out/throws.txt 2>&1; echo "(код завершення: $$?)"
> @head -n 1 target/out/throws.txt; grep "Exp1Checked" target/out/throws.txt

unchecked: build ## Дослід 1: неперевірювані винятки компілюються, падають під час виконання
> @echo; echo "Крок 5. Код із діленням на нуль і виходом за межі масиву. Очікувано: компілюється без помилок, код 0"
> @mkdir -p target/ce target/out
> $(T)/javac.sh -encoding UTF-8 -d target/ce src/main/java/ua/duikt/lavrishko/pr5/*.java; echo "(код завершення: $$?)"
> @echo; echo "Крок 6. Ділення на нуль. Очікувано: ArithmeticException: / by zero, код 1"
> $(J) exp1 div 0 > target/out/div.txt 2>&1; echo "(код завершення: $$?)"
> @head -n 1 target/out/div.txt
> @echo; echo "Крок 7. Індекс 5 у масиві з трьох елементів. Очікувано: ArrayIndexOutOfBoundsException, код 1"
> $(J) exp1 array 5 > target/out/array.txt 2>&1; echo "(код завершення: $$?)"
> @head -n 1 target/out/array.txt

order: build ## Дослід 1: catch (Exception) перед catch (IOException)
> @echo; echo "Крок 8. Хибний порядок catch. Очікувано: exception IOException has already been caught"
> @mkdir -p target/ce
> $(JC) compile-errors/E2_catch_order/*.java; echo "(код завершення: $$?)"

finally: build ## Дослід 1: finally виконується і з помилкою, і без
> @echo; echo "Крок 9. Блок finally. Очікувано: «Блок finally виконано» в обох запусках"
> $(J) exp1 finally data/stock.txt
> $(J) exp1 finally nofile.txt

cause: build ## Дослід 2: власний виняток без причини й з причиною
> @echo; echo "Крок 10. InvalidQuantityException без cause. Очікувано: getCause(): null"
> $(J) exp2 no-cause abc
> @echo; echo "Крок 11. InvalidQuantityException з cause. Очікувано: Caused by: java.lang.NumberFormatException"
> $(J) exp2 with-cause abc

files: build ## Дослід 3: запис без close() і з try-with-resources, читання в різних кодуваннях
> @echo; echo "Крок 12. Запис без close(). Очікувано: файл порожній, 0 байтів"
> @mkdir -p target/files
> $(J) exp3 write-noclose target/files/no_close.txt
> wc -c target/files/no_close.txt
> @echo; echo "Крок 13. Запис у try-with-resources. Очікувано: 83 символи, 135 байтів, три рядки"
> $(J) exp3 write-twr target/files/with_close.txt
> wc -m -c target/files/with_close.txt
> cat target/files/with_close.txt
> @echo; echo "Крок 14. Читання UTF-8 і windows-1251. Очікувано: у першому випадку нормальний текст, у другому «РџСЂРёРІС–С‚...»"
> $(J) exp3 read target/files/with_close.txt UTF-8
> $(J) exp3 read target/files/with_close.txt windows-1251

clean: ## видалити тимчасові файли (target/ce, target/out, target/files)
> rm -rf target/ce target/out target/files

show: build checked unchecked order finally cause files clean ## усе по черзі
> @echo; echo "Готово."

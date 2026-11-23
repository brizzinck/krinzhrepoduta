# ЛР 5: утиліта імпорту залишків (винятки, захисне програмування, потокове читання). Повний показ: make show
.RECIPEPREFIX := >
.PHONY: help build correct damaged large missing nodir encoding memory cause verify clean show
I := importer
T := ../../../tools
JAVA := cd $(I) && $(T)/java.sh
JAR := -jar target/importer.jar

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

$(I)/target/importer.jar: $(I)/pom.xml $(shell find $(I)/src -name '*.java')
> cd $(I) && $(T)/mvn.sh -q package

build: $(I)/target/importer.jar ## зібрати утиліту (Maven у docker), importer/target/importer.jar

$(I)/target/demo/CauseDemo.class: $(I)/target/importer.jar $(I)/demo/CauseDemo.java $(I)/demo/ReadAllDemo.java
> cd $(I) && $(T)/javac.sh -encoding UTF-8 -cp target/classes -d target/demo demo/ReadAllDemo.java demo/CauseDemo.java

correct: build ## Крок 1: коректний файл, 20 рядків
> @echo; echo "Крок 1. Коректний файл v2_correct.txt. Очікувано: Імпортовано: 20, відхилено: 0, вартість 10409337.34"
> @mkdir -p $(I)/target/out
> $(JAVA) $(JAR) data/v2_correct.txt target/out/report_correct.txt; echo "(код завершення: $$?)"

damaged: build ## Крок 2: пошкоджений файл, 8 відхилених рядків
> @echo; echo "Крок 2. Пошкоджений файл v2_damaged.txt. Очікувано: вісім рядків з номером і причиною, Імпортовано: 17, відхилено: 8"
> @mkdir -p $(I)/target/out
> $(JAVA) $(JAR) data/v2_damaged.txt target/out/report_damaged.txt; echo "(код завершення: $$?)"
> @echo; echo "Файл звіту target/out/report_damaged.txt:"
> @cat $(I)/target/out/report_damaged.txt

large: build ## Крок 3: файл на 200 000 рядків, консоль показує перші 20 відхилених
> @echo; echo "Крок 3. Великий файл v2_large.txt. Очікувано: перші 20 відхилених, «... і ще 77», Імпортовано: 199903, відхилено: 97"
> @mkdir -p $(I)/target/out
> $(JAVA) $(JAR) data/v2_large.txt target/out/report_large.txt; echo "(код завершення: $$?)"

missing: build ## Крок 4: немає вхідного файлу, повідомлення без стектрейсу
> @echo; echo "Крок 4. Вхідного файлу немає. Очікувано: Помилка: файл nofile.txt не знайдено, причина NoSuchFileException, код 1"
> $(JAVA) $(JAR) nofile.txt target/out/report_nofile.txt; echo "(код завершення: $$?)"

nodir: build ## Крок 5: звіт у теку, якої немає
> @echo; echo "Крок 5. Звіт у неіснуючу теку. Очікувано: Помилка: неможливо створити звіт ... теки не існує, код 1"
> $(JAVA) $(JAR) data/v2_correct.txt /nonexistent_dir/report.txt; echo "(код завершення: $$?)"

encoding: build ## Крок 6: файл у windows-1251 і явне кодування UTF-8
> @echo; echo "Крок 6. Файл у windows-1251. Очікувано: не в кодуванні UTF-8, MalformedInputException, звіт не створено"
> @mkdir -p $(I)/target/out; rm -f $(I)/target/out/report_cp1251.txt
> $(JAVA) $(JAR) demo/v2_cp1251.txt target/out/report_cp1251.txt; echo "(код завершення: $$?)"
> ls $(I)/target/out/report_cp1251.txt; echo "(код завершення: $$?)"

memory: $(I)/target/demo/CauseDemo.class ## Крок 7: потокове читання проти readAllLines при -Xmx8m
> @echo; echo "Крок 7. Купа 8 МБ. Очікувано: утиліта обробляє 200 000 рядків, readAllLines падає з OutOfMemoryError, з 64 МБ працює"
> @mkdir -p $(I)/target/out
> $(JAVA) -Xmx8m $(JAR) data/v2_large.txt target/out/report_large_8m.txt > target/out/m8.txt 2>&1; echo "(код завершення: $$?)"
> @tail -n 1 $(I)/target/out/m8.txt
> $(JAVA) -Xmx8m -cp target/demo ReadAllDemo data/v2_large.txt > target/out/oom.txt 2>&1; echo "(код завершення: $$?)"
> @grep -E "OutOfMemoryError|readAllLines" $(I)/target/out/oom.txt
> $(JAVA) -Xmx64m -cp target/demo ReadAllDemo data/v2_large.txt; echo "(код завершення: $$?)"

cause: $(I)/target/demo/CauseDemo.class ## Крок 8: ланцюжок винятків (cause)
> @echo; echo "Крок 8. Ланцюжок винятків. Очікувано: RecordFormatException з причиною NumberFormatException"
> $(JAVA) -cp target/classes:target/demo CauseDemo

verify: ## Крок 9: незалежний перерахунок підсумків (Python, Decimal)
> @echo; echo "Крок 9. Перерахунок окремою програмою. Очікувано: 20 / 10409337.34, 17 + 8 / 11627064.32, 199903 + 97 / 124872432674.03"
> docker run --rm -u "$$(id -u):$$(id -g)" -v "$$PWD/$(I)":/w -w /w python:3.12-slim python3 -I demo/verify.py

clean: ## видалити тимчасові файли (importer/target/out, importer/target/demo)
> rm -rf $(I)/target/out $(I)/target/demo

show: build correct damaged large missing nodir encoding memory cause verify clean ## усе по черзі
> @echo; echo "Готово."

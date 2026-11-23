# ЛР 2: інкапсуляція, конструктори, static (група й студент). Повний показ: make show
.RECIPEPREFIX := >
.PHONY: help build run e1 e2 e3 e4 e5 ok errors show
T := ../../tools
CP := -encoding UTF-8 -cp target/classes -d /tmp/out

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

build: ## зібрати проєкт Maven у docker (target/classes)
> @$(T)/mvn.sh -q clean package

run: build ## Main: підсумок, перелік, спроби порушити стан
> @echo; echo "Крок 1. Main: середній бал 62.50, потім 82.50 після зміни оцінки, 86.67 після видалення. Очікувано: спроби порушити стан відхилено"
> @$(T)/java.sh -cp target/classes ua.duikt.lavrishko.group.app.Main

e1: build ## E1: student.id = 99 з пакета app
> @echo; echo "Крок 2. E1: student.id = 99. Очікувано: id has private access in Student"
> @$(T)/javac.sh $(CP) compile-errors/E1_id_access/*.java || true

e2: build ## E2: student.attach(group) з пакета app
> @echo; echo "Крок 3. E2: student.attach(group). Очікувано: attach(StudyGroup) is not public in Student"
> @$(T)/javac.sh $(CP) compile-errors/E2_service_op/*.java || true

e3: build ## E3: group.students.add(...) з пакета app
> @echo; echo "Крок 4. E3: group.students.add(...). Очікувано: students has private access in StudyGroup"
> @$(T)/javac.sh $(CP) compile-errors/E3_internal_list/*.java || true

e4: build ## E4: присвоєння final-полю
> @echo; echo "Крок 5. E4: this.fullName = newName у методі. Очікувано: cannot assign a value to final variable fullName"
> @$(T)/javac.sh $(CP) compile-errors/E4_final_assign/*.java || true

e5: build ## E5: class Sneaky extends Student
> @echo; echo "Крок 6. E5: підклас Student. Очікувано: cannot inherit from final Student"
> @$(T)/javac.sh $(CP) compile-errors/E5_subclass/*.java || true

ok: build ## робочий код компілюється без помилок
> @echo; echo "Крок 7. Робочий код проєкту. Очікувано: без помилок"
> @$(T)/javac.sh -encoding UTF-8 -d /tmp/out $$(find src/main/java -name '*.java') && echo "Компіляція без помилок"

errors: e1 e2 e3 e4 e5 ## усі п'ять спроб, що не компілюються

show: run errors ok ## усе по черзі
> @echo; echo "Готово."

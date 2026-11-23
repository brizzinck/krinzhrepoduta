# ПЗ 4: абстрактний клас, інтерфейси, default-методи, передача залежності через конструктор. Повний показ: make show
.RECIPEPREFIX := >
.PHONY: help build abstract interfaces conflict di clean show
T := ../../tools
P := ua.duikt.lavrishko.pr4
JC := $(T)/javac.sh -encoding UTF-8 -d target/ce -cp target/classes
J := $(T)/java.sh -cp target/classes
SVC := src/main/java/ua/duikt/lavrishko/pr4/exp3

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

target/classes/$(subst .,/,$(P))/exp1/Main.class: pom.xml $(shell find src -name '*.java')
> $(T)/mvn.sh -q package

build: target/classes/$(subst .,/,$(P))/exp1/Main.class ## зібрати проєкт (Maven у docker) у target/

abstract: build ## Дослід 1: new для абстрактного класу, клас без render(), робочий варіант
> @echo; echo "Крок 1. Абстрактний клас Document. Очікувано: Document is abstract; cannot be instantiated, потім does not override abstract method render(), потім код 0 і вивід Main"
> @mkdir -p target/ce
> $(JC) compile-errors/E1_new_abstract/*.java; echo "(код завершення: $$?)"
> $(JC) compile-errors/E2_not_implemented/*.java; echo "(код завершення: $$?)"
> $(JC) compile-errors/E2b_implemented/*.java; echo "(код завершення: $$?)"
> $(J) $(P).exp1.Main

interfaces: build ## Дослід 2: через посилання на інтерфейс доступні лише його методи
> @echo; echo "Крок 2. Посилання на інтерфейс. Очікувано: cannot find symbol (p.export() для Printable, e.print() для Exportable)"
> @mkdir -p target/ce
> $(JC) compile-errors/E3_interface_view/*.java; echo "(код завершення: $$?)"
> $(JC) compile-errors/E3b_exportable_view/*.java; echo "(код завершення: $$?)"

conflict: build ## Дослід 2: конфлікт default describe() і його розв'язання
> @echo; echo "Крок 3. Конфлікт default-методів. Очікувано: types Printable and Exportable are incompatible, потім вивід із Printable.super та Exportable.super"
> @mkdir -p target/ce
> $(JC) compile-errors/E4_default_conflict/*.java; echo "(код завершення: $$?)"
> $(J) $(P).exp2.Main

di: build ## Дослід 3: new у сервісі проти залежності через конструктор
> @echo; echo "Крок 4. Версія 1: сервіс сам створює EmailNotifier. Очікувано: Email: ..."
> $(J) $(P).exp3.before.Main
> @echo; echo "Крок 5. Заміна на SMS у версії 1 вимагає правки сервісу. Очікувано: diff із -EmailNotifier і +SmsNotifier"
> diff -u $(SVC)/before/NotificationService.java $(SVC)/naive/NotificationService.java || true
> @echo; echo "Крок 6. Версія 2: залежність через конструктор. Очікувано: Email: ..., SMS: ..."
> $(J) $(P).exp3.after.Main
> @echo; echo "Крок 7. Сервіс не змінювався після додавання SmsNotifier. Очікувано: однакові хеші, diff порожній"
> sha256sum snapshots/NotificationService.stage1.java $(SVC)/after/NotificationService.java
> diff snapshots/NotificationService.stage1.java $(SVC)/after/NotificationService.java && echo "diff порожній"

clean: ## видалити тимчасові файли (target/ce)
> rm -rf target/ce

show: build abstract interfaces conflict di clean ## усе по черзі
> @echo; echo "Готово."

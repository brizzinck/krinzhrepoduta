# ЛР 3: поліморфізм і LSP (Медіаконтент: Фільм, Пісня, Подкаст). Повний показ: make show
.RECIPEPREFIX := >
.PHONY: help build stage1 stage2 evidence e1 e2 e3 e4 errors show
T := ../../tools
P := ua.duikt.lavrishko.media
A := snapshots/stage1_three_kinds/ua/duikt/lavrishko/media
B := src/main/java/ua/duikt/lavrishko/media
CP := -encoding UTF-8 -cp target/classes -d /tmp/out

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

build: ## зібрати проєкт Maven у docker (target/classes)
> @$(T)/mvn.sh -q clean package

stage1: ## етап 1: три різновиди, код зі snapshots/stage1_three_kinds
> @echo; echo "Крок 1. Етап 1: три різновиди (Film, Song, Podcast), код зі snapshots/stage1_three_kinds. Очікувано: Елементів: 3"
> @$(T)/javac.sh -encoding UTF-8 -d target/stage1 $$(find snapshots/stage1_three_kinds -name '*.java')
> @$(T)/java.sh -cp target/stage1 $(P).Main

stage2: build ## етап 2: додано Audiobook, той самий LibraryReport
> @echo; echo "Крок 2. Етап 2: додано Audiobook, LibraryReport той самий. Очікувано: Елементів: 4, Аудіокнига в переліку"
> @$(T)/java.sh -cp target/classes $(P).Main

evidence: ## докази: diff, sha256, відсутність instanceof
> @echo; echo "Крок 3. Докази, що код обробки не змінився. Очікувано: різняться лише Main і новий Audiobook, однакові sha256, grep без збігів (код 1)"
> @diff -rq snapshots/stage1_three_kinds src/main/java || true
> @sha256sum $(A)/LibraryReport.java $(B)/LibraryReport.java
> @G='instanceof|getClass\(\)|\((Film|Song|Podcast|Audiobook)\) *[a-z]'; grep -rnE "$$G" src/main/java; echo "grep, код завершення: $$?"

e1: build ## E1: новий різновид перевизначає final describe()
> @echo; echo "Крок 4. E1: BadMedia перевизначає describe(). Очікувано: overridden method is final"
> @$(T)/javac.sh $(CP) compile-errors/E1_override_final/*.java || true

e2: build ## E2: new MediaContent(...) для абстрактного типу
> @echo; echo "Крок 5. E2: new MediaContent(...). Очікувано: MediaContent is abstract; cannot be instantiated"
> @$(T)/javac.sh $(CP) compile-errors/E2_abstract/*.java || true

e3: build ## E3: різновид без kind() і details()
> @echo; echo "Крок 6. E3: Clip без kind() і details(). Очікувано: is not abstract and does not override abstract method"
> @$(T)/javac.sh $(CP) compile-errors/E3_missing_details/*.java || true

e4: build ## E4: class BadFilm extends Film
> @echo; echo "Крок 7. E4: успадкування від Film. Очікувано: cannot inherit from final Film"
> @$(T)/javac.sh $(CP) compile-errors/E4_extends_final/*.java || true

errors: e1 e2 e3 e4 ## усі чотири хибні кейси

show: stage1 stage2 evidence errors ## усе по черзі
> @echo; echo "Готово."

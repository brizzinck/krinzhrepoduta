# ПР 1: запуск і показ

Потрібен лише Docker (образи `maven:3.9-eclipse-temurin-21`, залежності Maven беруться з тома `duikt-m2`). Усі команди з теки `java/pr-1` репозиторію. Збірка пишеться в `target/`, файли репозиторію не змінюються.

## Пайплайн

```sh
cd java/pr-1   # тека роботи відносно кореня репозиторію
make version    # версії java і javac
make build      # mvn clean package
make manifest   # маніфест JAR
make run        # java -jar target/env-check-1.0.jar
make nolib      # JAR без теки lib: NoClassDefFoundError
make encoding   # локаль POSIX: кирилиця як ?
make javap      # байт-код методу greet()
```

`make show` виконує все по черзі, `make help` показує цілі, `make clean` видаляє `target/`. Цілі після `build` самі запускають збірку, якщо JAR ще немає.

## Кроки

- `version`: `../../tools/java.sh -version` і `../../tools/javac.sh -version` (JDK 21 у docker).
- `build`: `../../tools/mvn.sh clean package`, проєкт `pom.xml` із залежністю `commons-lang3` 3.17.0 і плагінами `maven-jar-plugin`, `maven-dependency-plugin`; показано рядки про компіляцію, копіювання залежностей, JAR і `BUILD SUCCESS`.
- `manifest`: `jar xf` усередині контейнера читає `META-INF/MANIFEST.MF` з `target/env-check-1.0.jar`.
- `run`: клас `ua.duikt.lavrishko.env.Main` (`src/main/java/.../Main.java`).
- `nolib`: копія JAR у `target/nolib/` без теки `lib`, тому клас `StringUtils` не знаходиться.
- `encoding`: той самий JAR із `LC_ALL=POSIX` і з типовою UTF-8 локаллю образу.
- `javap`: `javap -c -p` для класу `Main`, показано лише метод `greet()`.

## Що показати

1. `make version`: `openjdk version "21.0.12.1" 2026-08-18 LTS` і `javac 21.0.12.1`.
2. `make build`: `[INFO] Building jar: /w/target/env-check-1.0.jar`, потім `copy-dependencies` і `[INFO] BUILD SUCCESS`.
3. `make manifest`: `Class-Path: lib/commons-lang3-3.17.0.jar` і `Main-Class: ua.duikt.lavrishko.env.Main`. Без цих двох записів JAR не запуститься (див. наступний крок).
4. `make run`:
   ```text
   JVM: 21.0.12.1+1-LTS (Eclipse Adoptium)
   OS: Linux (amd64)
   Reversed (Commons Lang): avaJ
   Привіт зі збірки!
   ```
   Третій рядок дає метод із залежності, четвертий показує кирилицю без спотворень.
5. `make nolib`: `Exception in thread "main" java.lang.NoClassDefFoundError: org/apache/commons/lang3/StringUtils`. Звичайний JAR містить лише власні класи, залежності йдуть окремо через `Class-Path`.
6. `make encoding`: `?????? ?? ??????!` при `LC_ALL=POSIX`, `Привіт зі збірки!` при UTF-8. Кодування виводу JVM бере з локалі.
7. `make javap`: чотири інструкції `greet()`: `getstatic #7` (поле `System.out`), `ldc #60` (рядкова константа з пулу), `invokevirtual #35` (`println`), `return`.

## Звідки що береться

- `pom.xml`: координати `ua.duikt.lavrishko:env-check:1.0`, `release 21`, залежність і плагіни.
- `src/main/java/ua/duikt/lavrishko/env/Main.java`: код програми.
- `../../tools/*.sh`: запуск `java`, `javac` і Maven у docker.
- `results/`: збережений вивід цих самих команд, на який посилається звіт.

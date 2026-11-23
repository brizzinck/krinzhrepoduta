# ЛР 1: запуск і показ

Потрібен лише Docker (образи `maven:3.9-eclipse-temurin-21` і `eclipse-temurin:21-jre`, залежності Maven беруться з тома `duikt-m2`). Усі команди з теки `java/lr-1` репозиторію. Збірка пишеться в `target/`, файли репозиторію не змінюються.

## Пайплайн

```sh
cd java/lr-1   # тека роботи відносно кореня репозиторію
make build        # mvn clean package: 27 тестів і fat-jar
make table        # таблиця коефіцієнтів, усі напрямки
make session      # сеанс меню
make errors       # хибне введення без винятків
make multiword    # два слова в одному рядку
make fatjar       # Main-Class і commons-lang3 усередині JAR
make standalone   # запуск в образі лише з JRE
make incidents    # звичайний JAR: помилки без маніфеста й залежності
make nocatch      # у коді немає try і catch
```

`make show` виконує все по черзі, `make help` показує цілі, `make clean` видаляє `target/`. Цілі після `build` самі запускають збірку, якщо `target/converter.jar` ще немає.

## Кроки

- `build`: `../../tools/mvn.sh clean package`. `maven-shade-plugin` 3.5.1 пакує `target/converter.jar`, тести лежать у `src/test/java` (`ConverterTest`, `MenuTest`).
- `table`: `java -jar target/converter.jar --table`, клас `Main` друкує таблицю через `Converter`.
- `session`, `errors`, `multiword`: вхід передається в `stdin` через `printf`, меню читає `Menu.java` (`hasNextInt`, `hasNextDouble`, `dropRestOfLine`).
- `fatjar`: `jar xf` і `jar tf` усередині контейнера рахують записи `org/apache/commons/lang3/` у `converter.jar` і `original-converter.jar`.
- `standalone`: контейнер `eclipse-temurin:21-jre` без Maven отримує лише один файл `converter.jar`.
- `incidents`: `target/original-converter.jar` (JAR до обробки плагіном) запускається двома способами.
- `nocatch`: `grep -rc -E 'catch|try' src/main/java`.

## Що показати

1. `make build`: `Tests run: 27, Failures: 0, Errors: 0, Skipped: 0`, `Building jar: /w/target/converter.jar`, `BUILD SUCCESS`.
2. `make table`: рядок `acre  40468564.224   4046.8564224   40.468564224   0.40468564224  1`. Значення збігаються з незалежним еталоном із `verify/reference.py`.
3. `make session`: `100 cm² = 0.01 m²`, `1 ha = 2.47105381467 acre`, на невідому одиницю `м2` відповідь `Помилка: такої одиниці немає. Спробуйте ще раз.`, після `7` у головному меню `Помилка: такого пункту немає. Спробуйте ще раз.`
4. `make errors`: для `abc`, `NaN`, `1e999`, `12,5` повідомлення `Помилка: введіть число. Спробуйте ще раз.`, для `-5` `Помилка: площа не може бути від'ємною. Спробуйте ще раз.`, для `1e308 acre` у cm² `Помилка: результат завеликий. Спробуйте менше значення.` Стектрейсу немає.
5. `make multiword`: `7 8`, `-5 -6` і `foo bar` дають по одному повідомленню, далі `5 m² = 0.0005 ha`.
6. `make fatjar`: `Main-Class: ua.duikt.lavrishko.converter.Main`, `converter.jar: 413`, `original-converter.jar: 0`.
7. `make standalone`: останній рядок таблиці `acre  40468564.224   4046.8564224   40.468564224   0.40468564224  1`, хоча в контейнері є тільки JRE і цей файл.
8. `make incidents`: `no main manifest attribute, in target/original-converter.jar` і `NoClassDefFoundError: org/apache/commons/lang3/StringUtils`.
9. `make nocatch`: `:0` після кожного файла.

## Звідки що береться

- `src/main/java/ua/duikt/lavrishko/converter/`: `AreaUnit`, `Converter`, `Menu`, `Main`.
- `pom.xml`: `finalName` `converter`, залежність `commons-lang3`, плагіни `surefire` і `shade`.
- `../../tools/*.sh`: запуск `java` і Maven у docker.
- `diagrams/classes.puml`, `results/`: діаграма класів і збережений вивід, на які посилається звіт.

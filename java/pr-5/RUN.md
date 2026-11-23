# ПР 5: запуск і показ

Потрібен лише Docker. Усі команди з теки `java/pr-5`. Збірка йде в Maven і JDK 21 у docker (`../../tools/*.sh`), результати лише в `target/`, файли репозиторію `make` не змінює.

## Пайплайн

```sh
cd java/pr-5   # шлях відносно кореня репозиторію
make build        # mvn package
make checked      # перевірюваний виняток: помилка компілятора, try-catch, throws
make unchecked    # неперевірювані: ділення на нуль, вихід за межі масиву
make order        # catch (Exception) перед catch (IOException)
make finally      # блок finally
make cause        # власний виняток без причини і з причиною
make files        # запис без close() і з try-with-resources, кодування
```

`make show` виконує все по черзі й видаляє тимчасові файли, `make help` показує цілі.

## Кроки

- `make checked`: `javac` на `compile-errors/E1_unreported` і `compile-errors/fixed`, потім `Main exp1 catch nofile.txt` і `Main exp1 throws nofile.txt` (клас `Exp1Checked`).
- `make unchecked`: `javac` на весь `src/main/java/ua/duikt/lavrishko/pr5`, потім `Main exp1 div 0` і `Main exp1 array 5`.
- `make order`: `javac` на `compile-errors/E2_catch_order/WrongOrder.java`.
- `make finally`: `Main exp1 finally data/stock.txt` і `Main exp1 finally nofile.txt`.
- `make cause`: `Main exp2 no-cause abc` і `Main exp2 with-cause abc` (клас `Exp2Cause`, виняток `InvalidQuantityException`).
- `make files`: `Main exp3 write-noclose`, `write-twr`, `read ... UTF-8`, `read ... windows-1251` (клас `Exp3Files`); файли пишуться в `target/files`.

## Що показати

1. `make checked`: `error: unreported exception FileNotFoundException; must be caught or declared to be thrown`, код 1. Виправлені версії компілюються (код 0). `exp1 catch`: `Файл не знайдено: nofile.txt (No such file or directory)`, код 0. `exp1 throws`: `Exception in thread "main" java.io.FileNotFoundException`, код 1.
2. `make unchecked`: код компілюється (код 0), а під час виконання `ArithmeticException: / by zero` і `ArrayIndexOutOfBoundsException: Index 5 out of bounds for length 3`, у обох код 1.
3. `make order`: `error: exception IOException has already been caught`, код 1.
4. `make finally`: `Блок finally виконано` і для наявного файлу, і для `nofile.txt`.
5. `make cause`: без причини `getCause():   null`; з причиною є рядок `Caused by: java.lang.NumberFormatException: For input string: "abc"`.
6. `make files`: без `close()` `0 target/files/no_close.txt`; з `try-with-resources` ` 83 135 target/files/with_close.txt` і три рядки. Читання в UTF-8 дає `Привіт зі складу`, у windows-1251 `РџСЂРёРІС–С‚ Р·С– СЃРєР»Р°РґСѓ`.

## Звідки що береться

- `data/stock.txt`: невеликий вхідний файл для досліду з `finally`.
- `compile-errors/`: короткі програми з хибним кодом (`E1_unreported`, `E2_catch_order`) і виправлені версії (`fixed`).
- `compile-errors/run.sh` записує ті самі повідомлення компілятора у `results/` (їх вставлено в звіт).

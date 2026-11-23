# ЛР 8: запуск і показ

Потрібен лише Docker. Ручних кроків немає: `make show` на чистій машині сам встановлює jar підрядника в кеш Maven і докачує плагіни та JUnit. Мережа потрібна лише під час першого запуску (далі кеш уже є), а потім код підрядника виконується лише в контейнері без мережі (`--network none`) і з кешем лише для читання. Усі команди з теки `java/lr-8` (шлях від кореня репозиторію).

Jar підрядника лежить у `vendor/v2-vendor.jar` (файл `v2-vendor.jar` з Classroom, ЛР №8, варіант 2). У git його немає (`*.jar` в `.gitignore`), тому його треба покласти в `vendor/`. Без нього `make jar` зупиняється з повідомленням, де взяти файл.

## Пайплайн

```sh
cd java/lr-8
make jar      # встановити jar підрядника в кеш Maven, якщо його там ще немає
make build    # докачати кеш (мережа лише вперше) і скомпілювати тести
make api      # публічні сигнатури модуля (javap)
make good     # збірка 6: 35 тестів, жодного збою
make build1   # збірка 1
make build2   # збірка 2
make build3   # збірка 3
make build4   # збірка 4
make build5   # збірка 5
make zones    # інші часові пояси комп'ютера
make summary  # збережене зведення прогонів
make clean    # прибрати target/
```

Кожну ціль можна запускати окремо з чистого стану (потрібні для неї `jar`, `build` вона виконує сама); контейнерів після цілей не лишається. `make show` виконує все по черзі (разом із `jar`), `make help` показує цілі. Файли репозиторію не змінюються, Maven пише лише в `target/`; у виводі `make` помилок і рядків `(ignored)` немає, падіння тестів збірок 1-5 очікуване.

## Кроки

- `make jar`: якщо `ua.vendor:vendor:1.0` ще немає в томі `duikt-m2`, виконує `../../tools/mvn.sh install:install-file` із `vendor/v2-vendor.jar` (команда лише копіює файл).
- `make build`: при кожній збірці (мережа потрібна лише вперше, далі кеш уже є) `../../tools/mvn.sh test -Dtest=OverduePolicyTest#calendarAssumptions` (тест календаря, код підрядника не викликає) докачує плагіни й JUnit; потім `../../tools/mvn-sandbox.sh -q test-compile`, тести `src/test/java/ua/duikt/lavrishko/lr8/OverduePolicyTest.java`.
- `make api`: `javap -public` для `OverduePolicy`, `IssueStatus`, `Vendor` з jar підрядника.
- `make good`, `make build1` ... `make build5`: `mvn test -Dbuild=N`, номер збірки передає системна властивість `build`. Вивід скорочено до підсумку, назв тестів, що впали, і рядків `expected ... but was`.
- `make zones`: ті самі тести з `-DargLine=-Duser.timezone=...`.
- `make summary`: друкує `results/summary.txt`, зведення, зібране `sh summary.sh` з логів `results/`.

## Що показати

1. `make jar` і `make api`: `OverduePolicy.check(LocalDate, boolean)`, три значення `IssueStatus`, `Vendor.build(int, Clock)`. Тести написано за цими сигнатурами, код збірок не відкривався.
2. `make good`: `Tests run: 35, Failures: 0`. Збірку 6 рекомендовано прийняти.
3. `make build1`: `Tests run: 35, Failures: 6`, шість рядків `overdueByWorkingDays`. Вихідні враховано як робочі дні.
4. `make build2`: `Failures: 4`, `deadlineTodayOrLaterIsOnTrack[1]` і `[2]`, `kyivLateEveningWinter`, `dstDayLateEvening`. Дедлайн «сьогодні» вважається простроченим.
5. `make build3`: `Failures: 3`, `doneIssueIsOnTrack[1]`, `[2]`, `[3]`. Прапорець `done` ігнорується.
6. `make build4`: `Failures: 7`, сім рядків `overdueByWorkingDays`. Для `ESCALATE` потрібно 3 робочі дні замість 2.
7. `make build5`: `Failures: 3`, `kyivMidnightSummer`, `kyivDateEvenWhenClockIsInUtc`, `dstDayAfterMidnight`. Дата береться за UTC, а не за київським часом.
8. `make zones`: збірка 5 із поясом `Pacific/Kiritimati` падає на тих самих трьох тестах, збірка 6 із `America/Los_Angeles` дає `Failures: 0`. Тести не залежать від поясу комп'ютера.
9. `make summary`: порівняння наборів тестів, що впали, у звичайному запуску, у двох поясах і при випадковому порядку методів (зерна 1-3): скрізь `так`.

## Звідки що береться

- `vendor/v2-vendor.jar`: файл підрядника, поза git. SHA-256 починається з `b7ada15f`.
- `pom.xml`: JUnit 6.1.3, залежність підрядника зі scope `test`, `maven-surefire-plugin` 3.6.1.
- `../../tools/mvn-sandbox.sh`: Maven у docker з `--network none`, офлайн, том `duikt-m2` лише для читання.
- `results/`: збережені логи всіх прогонів (`build-1.txt` ... `build-6.txt`, `tz-*.txt`, `order.txt`, `summary.txt`), `run.sh` і `summary.sh` їх створюють і зводять.
- `diagrams/setup.puml` і `img/setup.png`: схема запуску з розділу 2 звіту.

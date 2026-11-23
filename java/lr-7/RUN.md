# ЛР 7: запуск і показ

Потрібен лише Docker (Maven і JDK 21 запускаються в контейнері). Усі команди з теки роботи (`java/lr-7` від кореня репозиторію).

## Пайплайн

```sh
cd java/lr-7
make build    # зібрати target/report.jar
make found    # звіт для наявного ключа SHOP-101
make missing  # звіт для відсутнього ключа SHOP-000
make usage    # запуск без ключа
make check    # перевірка підсумків без Java
```

`make show` виконує кроки `found`, `missing`, `usage`, `check` по черзі, `make help` показує цілі. Файли в репозиторії `make` не змінює: збірка йде в `target/`, вивід друкується в термінал.

## Кроки

- `make found` і `make missing`: `java -jar target/report.jar data/v2_issues.txt <ключ>`. Клас `ua.duikt.lavrishko.lr7.Main` читає файл через `Files.lines`, перетворює рядки на `Issue`, прибирає повтори (`distinct`), групує за статусом (`groupingBy` з `TreeMap`), бере топ-5 (`sorted`, `limit`) і шукає ключ (`findFirst`, `orElse`).
- `make usage`: той самий запуск без другого аргументу.
- `make check`: `tail`, `sort -u`, `awk` по `data/v2_issues.txt`, без Java.

## Що показати

1. `make found`: `Рядків у файлі: 40, унікальних записів: 36`, підсумки `DONE 11 77`, `IN_PROGRESS 9 36`, `REVIEW 8 33`, `TODO 8 35` (категорії за алфавітом), топ-5 з п'яти задач по 13 SP (`13 SHOP-190 DONE nova8` і далі), `Пошук SHOP-101: SHOP-101, DONE, виконавець byte8, 8 SP`.
2. `make missing`: ті самі підсумки і топ-5, `Пошук SHOP-000: не знайдено`. Виняток не виникає, `Optional` з `orElse`.
3. `make usage`: `Використання: java -jar report.jar <файл з даними> <ключ задачі>` і `(код завершення: 2)`.
4. `make check`: `40` рядків, `36` унікальних, `DONE 11 77`, `IN_PROGRESS 9 36`, `REVIEW 8 33`, `TODO 8 35`, шість задач із 13 SP (`SHOP-190`, `-286`, `-357`, `-366`, `-376`, `-395`). У топ-5 потрапили перші п'ять за ключем, `SHOP-395` шоста.

## Звідки що береться

- `data/v2_issues.txt`: дані варіанта 2 з Classroom.
- `src/main/java/ua/duikt/lavrishko/lr7/`: `Issue` і `Main`.
- `../../tools/mvn.sh`, `../../tools/java.sh`: Maven і Java 21 у docker.
- `run.sh`: те саме, але зберігає вивід у `results/`.

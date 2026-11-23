# ЛР 3: запуск і показ

Потрібен лише Docker. Усі команди з теки `java/lr-3` репозиторію. Збірка пише лише в `target/`, `javac` для хибних кейсів нічого не створює.

## Пайплайн

```sh
cd java/lr-3
make stage1    # етап 1: три різновиди, код зі snapshots/stage1_three_kinds
make stage2    # етап 2: додано Audiobook (збирає проєкт)
make evidence  # diff, sha256, відсутність instanceof
make e1        # BadMedia перевизначає final describe()
make e2        # new MediaContent(...)
make e3        # Clip без kind() і details()
make e4        # class BadFilm extends Film
```

`make errors` виконує `e1`..`e4` підряд, `make show` виконує все по черзі. `make help` показує всі цілі.

## Кроки

- `make stage1`: `javac` компілює `snapshots/stage1_three_kinds` у `target/stage1`, потім запускається `ua.duikt.lavrishko.media.Main` цього етапу.
- `make stage2`: `../../tools/mvn.sh -q clean package`, потім `Main` з `target/classes` (чотири різновиди).
- `make evidence`: `diff -rq snapshots/stage1_three_kinds src/main/java`, `sha256sum` обох `LibraryReport.java`, `grep` на `instanceof`, `getClass()` і приведення до різновидів по `src/main/java`.
- `make e1`..`make e4`: `javac` компілює файли з `compile-errors/E1_override_final`, `E2_abstract`, `E3_missing_details`, `E4_extends_final` проти `target/classes`.

## Що показати

1. `make stage1`: три різновиди через один `LibraryReport`, `Елементів: 3, загальна тривалість: 03:05:19`. Далі рядки `Відхилено: …`: порожня назва, тривалість 0, `рік виходу 1500 поза межами 1888..2100`, `номер випуску має бути більшим за 0, отримано 0`.
2. `make stage2`: `Бібліотека з новим різновидом (Audiobook):`, рядок `Аудіокнига «Вечори на хуторі», 05:43:00. Автор: Микола Гоголь, читає: Ігор Савчук.` і `Елементів: 4, загальна тривалість: 08:48:19`. Код обробки той самий.
3. `make evidence`: `diff -rq` називає лише `Audiobook.java` і `Main.java`; `sha256sum` дає однаковий хеш `dd41e3a5…293aad` для `LibraryReport.java` в обох етапах; `grep, код завершення: 1` означає, що `instanceof` і приведень немає.
4. `make e1`: `error: describe() in BadMedia cannot override describe() in MediaContent` і `overridden method is final`.
5. `make e2`: `error: MediaContent is abstract; cannot be instantiated`.
6. `make e3`: `error: Clip is not abstract and does not override abstract method details() in MediaContent`.
7. `make e4`: `error: cannot inherit from final Film`. Наприкінці `Готово.`

## Звідки що береться

- `src/main/java/ua/duikt/lavrishko/media`: `MediaContent` (спільні дані, `final describe()`, абстрактні `kind()` і `details()`), `Film`, `Song`, `Podcast`, `Audiobook` (усі `final`), `LibraryReport`, `Main`.
- `snapshots/stage1_three_kinds`: код етапу 1 без `Audiobook`; відрізняється від `src` лише `Main` і відсутнім `Audiobook`.
- `compile-errors/`: чотири хибні фрагменти. `compile-errors/run.sh` і `evidence.sh` записують вивід у `results/`, `make` лише друкує його.
- `../../tools/mvn.sh`, `../../tools/java.sh`, `../../tools/javac.sh`: Maven, `java` і `javac` (JDK 21) у docker.

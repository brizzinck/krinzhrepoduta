# ПЗ 4: запуск і показ

Потрібен лише Docker. Усі команди з теки `java/pz-4`. Збірка йде в Maven і JDK 21 у docker (`../../tools/*.sh`), результати лише в `target/`, файли репозиторію `make` не змінює.

## Пайплайн

```sh
cd java/pz-4   # шлях відносно кореня репозиторію
make build        # mvn package
make abstract     # дослід 1: абстрактний клас Document
make interfaces   # дослід 2: доступ через посилання на інтерфейс
make conflict     # дослід 2: конфлікт default describe()
make di           # дослід 3: new у сервісі проти конструктора
```

`make show` виконує все по черзі й видаляє тимчасові файли, `make help` показує цілі.

## Кроки

- `make abstract`: `javac` на `compile-errors/E1_new_abstract`, `E2_not_implemented`, `E2b_implemented`, потім запуск `ua.duikt.lavrishko.pr4.exp1.Main` (`Document`, `Report`, `Letter`).
- `make interfaces`: `javac` на `compile-errors/E3_interface_view` і `E3b_exportable_view` (інтерфейси `Printable`, `Exportable`).
- `make conflict`: `javac` на `compile-errors/E4_default_conflict`, потім `exp2.Main` (клас `Report` розв'язує конфлікт через `Printable.super` і `Exportable.super`).
- `make di`: `exp3.before.Main`, `diff -u` між `exp3/before` і `exp3/naive`, `exp3.after.Main`, `sha256sum` і `diff` сервісу зі знімком `snapshots/NotificationService.stage1.java`.

## Що показати

1. `make abstract`: `error: Document is abstract; cannot be instantiated`, далі `NoRender is not abstract and does not override abstract method render() in Document` (обидва код 1), версія з `render()` компілюється (код 0), `Main` друкує `Документ «Підсумки семестру»` і лист для `Петренко О. І.`.
2. `make interfaces`: `cannot find symbol ... method export()` для змінної типу `Printable` і `method print()` для `Exportable`. Через посилання на інтерфейс видно лише його методи.
3. `make conflict`: `types Printable and Exportable are incompatible; ... inherits unrelated defaults for describe()`. Після явного перевизначення `describe()` повертає `Printable: документ можна надрукувати; Exportable: документ можна експортувати`.
4. `make di`, версія 1: `Email: Вітаємо, Олена! Документ готовий.`, а `diff -u` показує, що перехід на SMS вимагає змінити сам сервіс (`-... new EmailNotifier()`, `+... new SmsNotifier()`).
5. `make di`, версія 2: `Email: ...` і `SMS: ...` без змін сервісу. Хеш `b0345d26fcd6...dc7e506` для знімка етапу 1 і для поточного `NotificationService.java` однаковий, `diff порожній`.

## Звідки що береться

- `compile-errors/E*`: короткі програми з хибним кодом, що компілюються окремо від проєкту.
- `snapshots/NotificationService.stage1.java`: копія сервісу до додавання `SmsNotifier`.
- `run.sh` і `compile-errors/run.sh` записують ті самі виводи у `results/` (їх вставлено в звіт).

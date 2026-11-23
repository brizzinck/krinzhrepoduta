# ПР 7: запуск і показ

Потрібен лише Docker (Maven і JDK 21 запускаються в контейнері). Усі команди з теки роботи (`java/pr-7` від кореня репозиторію).

## Пайплайн

```sh
cd java/pr-7
make build     # зібрати проєкт у target/
make sort      # три способи сортування
make errors    # calls++ у лямбді не компілюється
make classes   # .class-файли дослідів
make sum       # цикл проти потоку
make peek      # peek з термінальною операцією і без неї
make optional  # findFirst, Optional, map і orElse
make get       # get() на порожньому Optional
```

`make show` виконує всі кроки по черзі, `make help` показує цілі. Файли в репозиторії `make` не змінює: збірка йде в `target/`, вивід друкується в термінал.

## Кроки

- `make sort`: `Exp1Sort`, сортування 9 задач анонімним класом, лямбдою і `Comparator.comparingInt(Issue::getStoryPoints)`.
- `make errors`: `javac` на `compile-errors/E1_calls`, спроба збільшити локальну змінну в лямбді.
- `make classes`: `ls target/classes/ua/duikt/lavrishko/pr7/`.
- `make sum`: `Exp2Sum`, сума story points задач з оцінкою не менше 5 циклом і потоком.
- `make peek`: `Exp2Peek terminal` (з `sum()`) і `Exp2Peek lazy` (потік у змінній без термінальної операції).
- `make optional`: `Exp3Optional`, пошук понад 100 SP і за умовою досліду 2.
- `make get`: `Exp3Get`, виклик `get()` без перевірки.

## Що показати

1. `make sort`: три списки з 9 задач збігаються, останній рядок `Порядок збігається: true`. Лямбда й `comparingInt` займають по одному рядку, анонімний клас шість.
2. `make errors`: `error: local variables referenced from a lambda expression must be final or effectively final`.
3. `make classes`: є `Exp1Sort$1.class` для анонімного класу, для лямбди окремого файла немає.
4. `make sum`: `цикл: 39   потік: 39`.
5. `make peek`: з `sum()` п'ять рядків `peek SHOP-101`, `-102`, `-104`, `-106`, `-108` і `сума: 39`. Без термінальної операції лише `Конвеєр створено, термінальної операції немає`, `peek` мовчить.
6. `make optional`: `Optional: Optional.empty`, `map(Issue::getKey).orElse: не знайдено`, далі `Optional[SHOP-101 (5 SP) Вхід через Google]` і `SHOP-101`.
7. `make get`: `java.util.NoSuchElementException: No value present` і `(код завершення: 1)`.

## Звідки що береться

- `src/main/java/ua/duikt/lavrishko/pr7/`: `Issue`, `Sample` (9 задач) і класи дослідів `Exp*`.
- `compile-errors/`: хибний фрагмент, який не входить у збірку.
- `../../tools/mvn.sh`, `../../tools/javac.sh`, `../../tools/java.sh`: Maven, `javac` і Java 21 у docker.
- `run.sh`: те саме, але зберігає вивід у `results/`.

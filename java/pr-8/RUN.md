# ПР 8: запуск і показ

Потрібен лише Docker. Maven і JDK 21 працюють у контейнері, кеш залежностей лежить у docker-томі `duikt-m2`. Ручних кроків немає: `make show` працює з чистого стану (том `duikt-m2` і `target/` створюються самі, на мережу потрібен доступ до Maven Central при першому запуску). Усі команди з теки `java/pr-8` (шлях від кореня репозиторію).

## Пайплайн

```sh
cd java/pr-8
make build   # компіляція
make test1   # SprintTest: три тести Given-When-Then
make break1  # CAPACITY = 30 у копії, падає один тест
make test2   # SprintBoundaryTest: межі
make break2  # <= замінено на < у копії, падає рядок [3]
make test3   # IssueServiceTest: стаб і мок
make break3  # у копії закоментовано notifier.send
make agent   # попередження про агента Mockito без argLine і з ним
make final   # усі 12 тестів
make clean   # прибрати target/
```

Кожну ціль можна запускати окремо з чистого стану (`build` вона виконує сама); контейнерів після цілей не лишається. `make show` виконує все по черзі й у кінці викликає `clean`; у виводі `make` рядків `(ignored)` і помилок немає, падіння тестів у кроках 3, 5, 7 очікуване, `make help` показує цілі. Зламані варіанти робляться в копії `target/demo`, файли репозиторію не змінюються.

## Кроки

- `make test1`: `mvn test -Dtest=SprintTest`, клас `Sprint` і три тести в `SprintTest`.
- `make break1`: у копії `Sprint.java` стоїть `CAPACITY = 30`.
- `make test2`: `mvn test -Dtest=SprintBoundaryTest`, `@CsvSource` на межах і `@ValueSource` з `assertThrows`, разом 7 прогонів.
- `make break2`: у копії `current + points < CAPACITY` замість `<=`.
- `make test3`: `mvn test -Dtest=IssueServiceTest`, `IssueService` з моками `TeamDirectory` (стаб) і `ChatNotifier` (мок).
- `make break3`: у копії `IssueService` виклик `notifier.send(login, text)` закоментовано.
- `make agent`: той самий тест із `pom-without-agent.xml` (без `argLine`) і з `pom.xml` (з `-javaagent`).
- `make final`: `mvn test` для всього проєкту.

## Що показати

1. `make test1`: `Tests run: 3, Failures: 0`.
2. `make break1`: `SprintTest.addsTaskWhenItFits:19 expected: <true> but was: <false>`, `Tests run: 3, Failures: 1`. Сума 35 не вміщується в 30, інші два тести проходять.
3. `make test2`: `Tests run: 7, Failures: 0`.
4. `make break2`: `SprintBoundaryTest.boundariesInsideRange:23 expected: <true> but was: <false>`, `Failures: 1`. Це рядок `[3]`, сума рівно 40: саме на межі `<` відрізняється від `<=`.
5. `make test3`: `Tests run: 2, Failures: 0`.
6. `make break3`: `Wanted but not invoked`, `notifier.send("olena", "Задачу на 5 SP додано в спринт")`, `Actually, there were zero interactions with this mock.`, `Failures: 2`. Мок показує, що виклику не було.
7. `make agent`: без `argLine` є `Mockito is currently self-attaching` і `A Java agent has been loaded dynamically`. З `pom.xml` `Рядків про агента: 0`.
8. `make final`: `Tests run: 12, Failures: 0`, `BUILD SUCCESS`.

## Звідки що береться

- `pom.xml`: JUnit 6.1.3 через `junit-bom`, `mockito-junit-jupiter` 5.24.0, Surefire 3.6.1, `argLine` з `-javaagent` для Mockito. `pom-without-agent.xml` той самий без `argLine`.
- `src/main/java/ua/duikt/lavrishko/pr8`: `Sprint`, `IssueService`, інтерфейси `TeamDirectory` і `ChatNotifier`.
- `src/test/java/ua/duikt/lavrishko/pr8`: `SprintTest`, `SprintBoundaryTest`, `IssueServiceTest`.
- `results/`: логи тих самих прогонів, які створює `run.sh`.
- `../../tools/mvn.sh`: Maven у docker.

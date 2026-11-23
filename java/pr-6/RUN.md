# ПР 6: запуск і показ

Потрібен лише Docker (Maven і JDK 21 запускаються в контейнері). Усі команди з теки роботи (`java/pr-6` від кореня репозиторію).

## Пайплайн

```sh
cd java/pr-6
make build     # зібрати проєкт у target/
make errors    # два хибні фрагменти, помилки javac
make cast      # ObjectBox: ClassCastException під час виконання
make total     # List<? extends Number>
make timing    # час ArrayList, LinkedList, HashSet
make cme       # видалення в for-each
make iterator  # видалення через Iterator
make equality  # HashSet, equals і hashCode, зміна ключа
```

`make show` виконує всі кроки по черзі, `make help` показує цілі. Файли в репозиторії `make` не змінює: збірка йде в `target/`, вивід друкується в термінал.

## Кроки

- `make errors`: `javac` на `compile-errors/E1_box_type` (рядок у `Box<Issue>`) і `compile-errors/E2_list_number` (`List<Integer>` у параметр `List<Number>`).
- `make cast`: клас `Exp1Cast` (`ObjectBox`, приведення `(Issue)`).
- `make total`: `Exp1Total` (`Generics.total`, `Generics.first`).
- `make timing`: `Exp2Timing`, 100 000 елементів, 1 000 вставок у середину і 1 000 `contains`, 5 запусків, час у мс із двома знаками.
- `make cme`: `Exp2Cme`, видалення зі `ArrayList` у циклі for-each.
- `make iterator`: `Exp2Iterator`, те саме через `Iterator.remove()`.
- `make equality`: `Exp3Equality`, три стани класу і зміна ключа елемента в `HashSet`.

## Що показати

1. `make errors`: `incompatible types: String cannot be converted to Issue` і `incompatible types: List<Integer> cannot be converted to List<Number>`. Помилки видно під час компіляції.
2. `make cast`: `Значення покладено, компіляція пройшла.`, далі `java.lang.ClassCastException: class java.lang.String cannot be cast to class ua.duikt.lavrishko.pr6.Issue`. Для `Object` помилка лише під час виконання.
3. `make total`: `total(List<Integer>) = 26.0`, `total(List<Number>)  = 26.5`. Підстановка `? extends Number` приймає обидва списки.
4. `make timing`: вставка в `LinkedList` у сотні мс (близько 160-320), у `ArrayList` одиниці мс. `contains` у списках сотні мс, у `HashSet` менше 1 мс (0.25-0.44). Числа від запуску до запуску різні, порядки величин стабільні.
5. `make cme`: `java.util.ConcurrentModificationException`, `(код завершення: 1)`.
6. `make iterator`: `До видалення: 8 задач`, `Після видалення: 5 задач`.
7. `make equality`: без `equals` і `hashCode` `size = 2, contains = false`, лише `equals` `size = 2, contains = false`, обидва методи `size = 1, contains = true`. Після `setKey(SHOP-2)` обидва `contains` дають `false`, хоча `size = 1`.

## Звідки що береться

- `src/main/java/ua/duikt/lavrishko/pr6/`: `Box`, `ObjectBox`, `Generics`, `Issue` і класи дослідів `Exp*`.
- `compile-errors/`: хибні фрагменти, які не входять у збірку.
- `../../tools/mvn.sh`, `../../tools/javac.sh`, `../../tools/java.sh`: Maven, `javac` і Java 21 у docker.
- `run.sh`: те саме, але зберігає вивід у `results/`.

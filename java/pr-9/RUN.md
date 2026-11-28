# ПР 9: запуск і показ

Потрібен лише Docker: `javac`, `java` і `jstack` працюють у контейнері образу `maven:3.9-eclipse-temurin-21` (JDK 21), бібліотек немає. Усі команди з теки роботи (`java/pr-9` від кореня репозиторію).

## Пайплайн

```sh
cd java/pr-9
make build     # скомпілювати класи в target/classes
make race      # дослід 1: без synchronized, три запуски
make synced    # дослід 1: з synchronized, три запуски
make deadlock  # дослід 2: зависання, jps, jstack
make fixed     # дослід 2: порядок за id, програма завершується
make clean     # прибрати target/
```

`make show` виконує все по черзі й у кінці викликає `clean`, `make help` показує цілі. Кожну ціль можна запускати окремо з чистого стану (`build` вона виконує сама). Файли репозиторію `make` не змінює.

## Кроки

- `make race`: `Exp1Race plain`, 16 потоків по 25 000 викликів `close()` без синхронізації. Очікується менше 400000 і щоразу інше число.
- `make synced`: `Exp1Race synced`, той самий код через `closeSynced()` (`synchronized`). Очікується тричі `400000 / 400000`.
- `make deadlock`: `Exp2Deadlock naive` запускається у фоновому контейнері `pr9-deadlock` (змінна `NAME`). Через 2 секунди `docker logs` показує два рядки «тримає дошку ... чекає дошку ...» без рядка про завершення, `docker ps` показує `Up`, `jps -l` дає PID 1, `jstack 1` показує `Found one Java-level deadlock`. Контейнер видаляється в кінці й після помилки чи Ctrl+C (`trap`), старий контейнер з таким іменем видаляється перед стартом. Команди в виводі ті самі, що реально виконуються: `docker logs`, `docker exec ... jps -l`, `docker exec ... jstack 1`.
- `make fixed`: `Exp2Deadlock ordered`, монітори беруться за зростанням `id`, друкується `Програма завершилась`.

## Що показати

1. `make race`: три різні числа менше 400000, у кожному рядку є `втрачено`.
2. `make synced`: тричі `400000 / 400000`.
3. `make deadlock`: два рядки «тримає ... чекає», статус `Up`, у дампі `"Thread-0"` чекає монітор, який тримає `"Thread-1"`, і навпаки.
4. `make fixed`: `Дошка 1: 10 задач, дошка 2: 10 задач, разом 20. Програма завершилась.`

Числа в `make race` відрізняються від таблиці у звіті й від запуску до запуску: це гонитва, правильного значення немає. Звіт спирається на `results/`.

## Звідки що береться

- `src/main/java/ua/duikt/lavrishko/pr9`: `SprintCounter` (два методи, лише слово `synchronized` різне), `Exp1Race`, `TaskBoard` (`moveTask` і `moveTaskOrdered`), `Exp2Deadlock`.
- `results/`: логи тих самих запусків, які створює `run.sh` (`exp1_plain.txt`, `exp1_synced.txt`, `exp2_deadlock.txt` з повним дампом, `exp2_ordered.txt`).
- `img/deadlock.png`: знімок терміналу з фрагментом дампу для звіту.
- `../../tools`: `javac.sh`, `java.sh`, `runlog.sh`.

# ПР 3.3 і ЛР 3.3: запуск і показ

Потрібен лише Docker. Усі команди з теки `homework/db/pr-3.3`.

## Пайплайн

```sh
cd /home/skalse/job/duikt/course_3/students/lavrishko_vladyslav/homework/db/pr-3.3
make migrate    # міграції Flyway від версії 1 до 2
make queries    # запити 3-6
make nplus1     # проблема N+1
make run        # меню вручну
```

`make show` виконує `migrate`, `queries` і `nplus1` підряд. `make help` показує всі цілі.

## Що показати

1. `make migrate`. Перший старт падає: `Schema-validation: missing column [Email]`. Сутність `Customers` має поле `email`, а в базі версії 1 колонки ще немає. Далі `Successfully baselined schema with version: 1`, `Successfully applied 1 migration ... now at version v2`, таблиця `info` (V1 `Baseline`, V2 `Success`) і чистий старт `Підключено до БД: northwind_orm`.
2. `make queries`. Запит 3: 8 категорій (Beverages `37.98`). Запит 4: 5 міст (London 4 і 6). Запит 5: QUICK-Stop `110277.31`, `Показано 10 з 89`. Запит 6: `10248 | 1996-07-04 | VINET | Steven Buchanan`, `Показано 10 з 830`.
3. `make nplus1`. `Ліниве завантаження: SQL-запитів 6`, `join fetch: SQL-запитів 1`.
4. За потреби `make tables`: усі 13 таблиць Northwind по черзі.

Код: `src/main/java/northwind/Queries.java` (запити JPQL), `src/main/resources/db/migration/V2__add_customer_email.sql` (міграція).

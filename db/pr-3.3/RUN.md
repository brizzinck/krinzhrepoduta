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

`make show` виконує `migrate`, `queries`, `nplus1` і `tables` підряд. `make help` показує всі цілі.

## SQL по кроках

- `make migrate`: `sql/01-flyway-v1-schema.sql`, посилання на `V1__northwind_schema.sql` (базова схема); `sql/02-flyway-v2-add-customer-email.sql`, міграція `ALTER TABLE customers ADD COLUMN Email`.
- `make queries`: `sql/03-query3-avg-price.sql`, `sql/04-query4-employees-customers-by-city.sql`, `sql/05-query5-revenue-by-customer.sql`, `sql/06-query6-orders-with-employee.sql`, JPQL і SQL, який згенерував Hibernate.
- `make nplus1`: `sql/07-n-plus-1.sql`, один запит списку й п'ять запитів на співробітників проти одного `join fetch`.
- `make tables`: `sql/08-tables-1-13.sql`, JPQL і згенерований SQL для кожної з 13 таблиць.
- `make validate`: `sql/02-flyway-v2-add-customer-email.sql`, колонка, якої бракує до V2.
- `make sql`: друкує весь вміст `sql/`. `make show` перед кожним кроком друкує потрібні файли через `cat`.

## Що показати

1. `make migrate`. Перший старт падає: `Schema-validation: missing column [Email]`. Сутність `Customers` має поле `email`, а в базі версії 1 колонки ще немає. Далі `Successfully baselined schema with version: 1`, `Successfully applied 1 migration ... now at version v2`, таблиця `info` (V1 `Baseline`, V2 `Success`) і чистий старт `Підключено до БД: northwind_orm`.
2. `make queries`. Запит 3: 8 категорій (Beverages `37.98`). Запит 4: 5 міст (London 4 і 6). Запит 5: QUICK-Stop `110277.31`, `Показано 10 з 89`. Запит 6: `10248 | 1996-07-04 | VINET | Steven Buchanan`, `Показано 10 з 830`.
3. `make nplus1`. `Ліниве завантаження: SQL-запитів 6`, `join fetch: SQL-запитів 1`.
4. За потреби `make tables`: усі 13 таблиць Northwind по черзі.

Код: `src/main/java/northwind/Queries.java` (запити JPQL), `src/main/resources/db/migration/V2__add_customer_email.sql` (міграція), SQL кожного кроку в `sql/`.

## Звідки що береться

`make` друкує кожну команду завантаження бази, наприклад `./mysql.sh < schema.sql`. `mysql.sh` запускає клієнт `mysql` у контейнері `duikt-mysql`, `mysql-up.sh` піднімає цей контейнер.

- `northwind_orm.sql`: база `northwind_orm`, Northwind викладачки (дамп `MY_SQL Labwork2.sql` з Classroom, ПР 1.2) і два виправлення в кінці файла: знижки з `LabWork2.sql` і `AUTO_INCREMENT` для `products`.

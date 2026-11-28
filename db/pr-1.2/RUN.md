# ПР 1.2: запуск і показ

Потрібен лише Docker. Усі команди з теки `homework/db/pr-1.2`.

## Пайплайн

```sh
cd /home/skalse/job/duikt/course_3/students/lavrishko_vladyslav/homework/db/pr-1.2
make db    # свіжа база northwind
make q1    # запит 1 ... make q7: сім запитів варіанта 2
```

## SQL по кроках

- `make q1`: `sql/01-products-over-20.sql`, продукти дорожчі за 20.
- `make q2`: `sql/02-orders-tuesday.sql`, замовлення у вівторок.
- `make q3`: `sql/03-avg-price-by-category.sql`, середня ціна по категоріях.
- `make q4`: `sql/04-employees-customers-by-city.sql`, співробітники й клієнти по містах.
- `make q5`: `sql/05-customer-revenue.sql`, дохід клієнтів.
- `make q6`: `sql/06-orders-employee-subquery.sql`, замовлення зі співробітником (підзапит).
- `make q7`: `sql/07-union-all-germany.sql`, `UNION ALL` постачальників і клієнтів Німеччини.

`make show` виконує `db` і `q1`-`q7` підряд. `make help` показує всі цілі.

## Що показати

1. `make q1`. Найдорожчий `Cote de Blaye | 263.5000`, внизу `37 rows in set`. Фільтр `WHERE` і сортування `ORDER BY`.
2. `make q2`. Перше замовлення `10252 | SUPRD | 1996-07-09 ... | Tuesday`, внизу `168 rows in set`. Вівторок це `DAYOFWEEK = 3`, неділя це 1.
3. `make q3`. Середня ціна по категоріях, `8 rows in set`. Групування `GROUP BY` із `JOIN`.
4. `make q4`. Kirkland 1 і 1, London 4 і 6, Redmond 1 і 0, Seattle 2 і 1, Tacoma 1 і 0, `5 rows in set`. `COUNT(DISTINCT ...)` потрібен, бо `JOIN` множить рядки (без нього в Лондоні було б 24), `LEFT JOIN` лишає міста без клієнтів.
5. `make q5`. Перший `QUICK-Stop | 110277.31`, далі `Ernst Handel | 104874.98`, `89 rows in set`. `INNER JOIN` відсікає двох клієнтів без замовлень.
6. `make q6`. `10248 | 1996-07-04 ... | VINET | Steven Buchanan`, `830 rows in set`. Скалярний підзапит у `SELECT`.
7. `make q7`. `QUICK-Stop | Customer` і постачальники в одному списку, `14 rows in set` (3 і 11). `UNION ALL` це множинна операція з умови.

Код: `sql/` (по файлу на запит), `queries.sql` (усі сім запитів разом, лишається як у звіті), `northwind.sql` (створення бази).

## Звідки що береться

`make` друкує кожну команду, наприклад `./mysql.sh -t -vvv --skip-comments northwind < sql/01-products-over-20.sql`; `-vvv` друкує кожен запит перед результатом. `mysql.sh` запускає клієнт `mysql` у контейнері `duikt-mysql`, `mysql-up.sh` піднімає цей контейнер.

- `northwind.sql`: база `northwind`, Northwind викладачки (дамп `MY_SQL Labwork2.sql` з Classroom, ПР 1.2) і два виправлення в кінці файла: знижки з `LabWork2.sql` і `AUTO_INCREMENT` для `products`.

`tidy.sh` лише скорочує вивід `mysql -vvv`: коментар із файла друкується одним рядком, запит після `mysql>`.

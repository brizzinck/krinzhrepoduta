# ЛР 1.2: запуск і показ

Потрібен лише Docker. Усі команди з теки `homework/db/lr-1.2`.

## Пайплайн

```sh
cd /home/skalse/job/duikt/course_3/students/lavrishko_vladyslav/homework/db/lr-1.2
make db    # свіжа база northwind
make c1    # SELECT, WHERE, DISTINCT, NULL, LIKE
make c2    # групування і JOIN
make c3    # множинні операції і підзапити
make c4    # топ-5 співробітників за кількістю замовлень
```

## SQL по кроках

- `make c1`: `sql/01-select-where.sql`, `DISTINCT`, `ORDER BY`, `NULL`, `BETWEEN`, `LIKE`, `IN`, функції.
- `make c2`: `sql/02-group-join.sql`, `GROUP BY`, `HAVING`, `CASE`, `JOIN`, емуляція `FULL JOIN` (прямий `FULL OUTER JOIN` дає `ERROR 1064`).
- `make c3`: `sql/03-sets-subq.sql`, `UNION`, `INTERSECT`, `EXCEPT`, `MINUS` (`ERROR 1064`) і підзапити.

- `make c4`: `sql/04-top-employees.sql`, топ-5 співробітників за кількістю оброблених замовлень (`JOIN`, `GROUP BY`, `COUNT`, `ORDER BY ... DESC`, `LIMIT 5`).

`make show` виконує `db`, `c1`, `c2`, `c3` і `c4` підряд. `make help` показує всі цілі.

## Що показати

1. `make c1`. Кожен запит і його результат: `DISTINCT`, `ORDER BY` (`5 rows in set`), сортування з `NULL` у `Region` (9 рядків), `BETWEEN`, `LIKE`, `IN` і функції `UPPER`, `LEFT`. Це перший блок чекліста лекції.
2. `make c2`. `GROUP BY`, `HAVING` і `CASE`, далі `inner_cnt 830`, `left_cnt 832`, клієнти без замовлень `FISSA` і `PARIS`, емуляція повного з'єднання `full_emul_cnt 832`, а прямий `FULL OUTER JOIN` дає `ERROR 1064`. У MySQL його немає, замінник це `LEFT JOIN UNION RIGHT JOIN`.
3. `make c3`. `union_all_cnt 120` проти `union_cnt 25` (`UNION` прибирає дублікати), `INTERSECT` і `EXCEPT` працюють (`4 rows in set`: Australia, Japan, Netherlands, Singapore), а `MINUS` дає `ERROR 1064`. Далі підзапити у `WHERE`, `HAVING`, `FROM` і `SELECT`.

Код: `sql/01-select-where.sql`, `sql/02-group-join.sql`, `sql/03-sets-subq.sql` (копії `shots/c1-...`, `shots/c2-...`, `shots/c3-...` для звіту).

## Звідки що береться

`make` друкує кожну команду, наприклад `./mysql.sh -t -vvv -n --force --skip-comments northwind < sql/01-select-where.sql`; `-vvv` друкує кожен запит перед результатом. `mysql.sh` запускає клієнт `mysql` у контейнері `duikt-mysql`, `mysql-up.sh` піднімає цей контейнер.

- `northwind.sql`: база `northwind`, Northwind викладачки (дамп `MY_SQL Labwork2.sql` з Classroom, ПР 1.2) і два виправлення в кінці файла: знижки з `LabWork2.sql` і `AUTO_INCREMENT` для `products`.

`tidy.sh` лише скорочує вивід `mysql -vvv`: коментар із файла друкується одним рядком, запит після `mysql>`.

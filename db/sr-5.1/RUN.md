# СР 5.1: запуск і показ

Потрібен лише Docker. Усі команди з теки `homework/db/sr-5.1`. База `car_rental` з ПР 1.1.

## Пайплайн

```sh
cd /home/skalse/job/duikt/course_3/students/lavrishko_vladyslav/homework/db/sr-5.1
make db         # база car_rental у вихідному стані
make union      # об'єднання
make intersect  # перетин
make except     # різниця
make project    # проєкція, вибірка, декартів добуток
make join       # види з'єднань
make full       # FULL OUTER JOIN
make divide     # ділення
make diff       # відмінності SQL від алгебри
```

## SQL по кроках

- `make union`: `sql/01-union.sql`, об'єднання UNION і UNION ALL.
- `make intersect`: `sql/02-intersect.sql`, перетин INTERSECT.
- `make except`: `sql/03-except.sql`, різниця EXCEPT, `MINUS` дає `ERROR 1064`.
- `make project`: `sql/04-projection-selection-product.sql`, проєкція, вибірка, декартів добуток.
- `make join`: `sql/05-joins.sql`, θ-, екві-, природне, напів- і антиз'єднання.
- `make full`: `sql/06-full-join.sql`, `FULL OUTER JOIN` (`ERROR 1064`) і його емуляція.
- `make divide`: `sql/07-division.sql`, ділення через подвійне `NOT EXISTS`.
- `make diff`: `sql/08-sql-vs-algebra.sql`, відмінності SQL від алгебри.

`make show` виконує все підряд (довгий вивід). `make help` показує всі цілі. Дві помилки `ERROR 1064` (`MINUS` і `FULL OUTER JOIN`) навмисні, вони є в оцінюваних прикладах.

## Що показати

1. `make union`. `rows_union 13`, `rows_union_all 20`. `UNION` прибирає дублікати, `UNION ALL` ні.
2. `make intersect`. Клієнти 1 і 8, нижче те саме через `R1 EXCEPT (R1 EXCEPT R2)`. Перетин виражається через різницю.
3. `make except`. Філія 3 мінус 4 дає 5 рядків, 4 мінус 3 дає 6. Потім `ERROR 1064` для `MINUS`. Різниця не комутативна, а `MINUS` у MySQL немає.
4. `make project`. `rows_comma 20` і `rows_cross_join 20`: 4 філії на 5 послуг. Проєкція з `DISTINCT` зводить 12 рядків до 10.
5. `make join`. `rows_inner 12` і `rows_natural 12`, `rows_inner_join 30` проти 18 клієнтів в `EXISTS`. Антиз'єднання: Вовк (19) і Левченко (20). Показує різницю між видами з'єднань.
6. `make full`. `ERROR 1064` для `FULL OUTER JOIN`, потім емуляція `LEFT JOIN UNION RIGHT JOIN`: `total_rows 32`, `unpaired_rows 7`.
7. `make divide`. Клієнти 1 (Коваленко) і 11 (Руденко) орендували і «Економ», і «Преміум». Ділення в SQL записується подвійним `NOT EXISTS`.
8. `make diff`. `rows_without_distinct 12`, `rows_with_distinct 10`, `eq_null 0`, `is_null 5`, `not_in_result 0`, `not_exists_result 2`. SQL працює з мультимножинами і `NULL`, алгебра ні.

Код: `sql/` (по файлу на крок), `examples.sql` (усі секції прикладів, лишається як у звіті).

## Звідки що береться

`make` друкує кожну команду, наприклад `./mysql.sh -t -vvv --force --skip-comments car_rental < sql/01-union.sql`; `-vvv` друкує кожен запит перед результатом. `mysql.sh` запускає клієнт `mysql` у контейнері `duikt-mysql`, `mysql-up.sh` піднімає цей контейнер.

- `schema.sql`, `seed.sql`: база `car_rental`, копія з ПР 1.1.

`tidy.sh` лише скорочує вивід `mysql -vvv`: коментар із файла друкується одним рядком, запит після `mysql>`.

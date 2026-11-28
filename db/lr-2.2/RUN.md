# ЛР 2.2: запуск і показ

Потрібен лише Docker. Усі команди з теки `homework/db/lr-2.2`. Працює на окремій копії `northwind_defense`.

## Пайплайн

```sh
cd /home/skalse/job/duikt/course_3/students/lavrishko_vladyslav/homework/db/lr-2.2
make db         # свіжа копія northwind_defense
make func       # YEAR() проти діапазону дат
make like       # LIKE '%a' проти 'A%'
make selectivity # Discontinued = 0 проти 1
make invisible  # INVISIBLE і VISIBLE індекс
make exists     # DISTINCT після JOIN проти EXISTS
```

`make defense` виконує п'ять останніх цілей підряд, `make show` виконує `db` і `defense`. `make help` показує всі цілі. Вивід широкий, дивись стовпці `type`, `key`, `rows`, `Extra`. Кожен крок сам перестворює базу, тому його можна запускати окремо й повторно; `make invisible` спершу виконує крок 1, бо використовує його індекс.

## SQL по кроках

Кожен крок виконує файл із `sql/`. `make` друкує команду, клієнт `mysql -vvv` перед кожним результатом друкує сам запит разом з коментарями `-- До`, `-- Зміна`, `-- Після`.

- `make func`: `sql/01-function-over-column.sql`, `CREATE INDEX idx_orders_date`; зміна: `YEAR(OrderDate) = 1997` переписано на діапазон дат.
- `make like`: `sql/02-like.sql`, `CREATE INDEX idx_customers_name`; зміна: `LIKE '%a'` замінено на `LIKE 'A%'`.
- `make selectivity`: `sql/03-selectivity.sql`, `CREATE INDEX idx_products_disc`; зміна: `Discontinued = 0` замінено на `= 1`.
- `make invisible`: `sql/04-invisible-index.sql`; зміна: `ALTER INDEX idx_orders_date INVISIBLE`, потім `VISIBLE`.
- `make exists`: `sql/05-distinct-exists.sql`; зміна: `SELECT DISTINCT ... JOIN` переписано на `WHERE EXISTS (...)`.

## Що показати

1. `make func`, функція над стовпцем. `YEAR(OrderDate) = 1997`: `type index`, `rows 830`, індекс читається повністю. Діапазон `OrderDate >= ... AND OrderDate < ...`: `type range`, `rows 408`. Функція заважає seek, діапазон ні.
2. `make like`, `LIKE`. `LIKE 'A%'`: `type range`, `rows 4`. `LIKE '%a'`: `type ALL`, `rows 91`. Початок рядка використовує індекс, початок із `%` ні.
3. `make selectivity`, селективність. `Discontinued`: `0 | 69` і `1 | 8`, обидва плани `ref`, `rows 69` і `rows 8`. Індекс узято в обох, але при 0 він майже нічого не відсіює.
4. `make invisible`, невидимий індекс. З `INVISIBLE`: `type ALL`, `rows 830`. Після `VISIBLE`: `type range`, `rows 33`. Видно план без індексу, не видаляючи його.
5. `make exists`, `DISTINCT` проти `EXISTS`. Плани майже однакові, у другому `Extra` має `FirstMatch(c)`: MySQL сам переписує `EXISTS` у напівз'єднання.

Кожен приклад друкує запит із підписом «До» або «Після» і його план. Код: `sql/` (ті самі запити з підписами) і `defense_queries.sql` (як здано).

## Звідки що береться

`make` друкує кожну команду завантаження бази, наприклад `./mysql.sh < schema.sql`. `mysql.sh` запускає клієнт `mysql` у контейнері `duikt-mysql`, `mysql-up.sh` піднімає цей контейнер.

- `northwind_defense.sql`: база `northwind_defense`, Northwind викладачки (дамп `MY_SQL Labwork2.sql` з Classroom, ПР 1.2) і два виправлення в кінці файла: знижки з `LabWork2.sql` і `AUTO_INCREMENT` для `products`.

`tidy.sh` лише скорочує вивід `mysql -vvv`: коментар із файла друкується одним рядком, запит після `mysql>`.

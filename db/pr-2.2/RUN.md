# ПР 2.2: запуск і показ

Потрібен лише Docker. Усі команди з теки `homework/db/pr-2.2`. Працює на базі `northwind_opt`, таблиця `test_index`.

## Пайплайн

```sh
cd /home/skalse/job/duikt/course_3/students/lavrishko_vladyslav/homework/db/pr-2.2
make db        # свіжа northwind_opt і службові процедури (крок 00)
make empty     # порожня таблиця без ключа
make one       # один рядок
make rows30    # 30 рядків
make newpage   # друга сторінка
make rows240   # 240 рядків
make pk        # PRIMARY KEY, 18630 рядків
make depth3    # третій рівень дерева
make seq       # послідовний ключ
make random    # випадковий ключ, фрагментація
make optimize  # OPTIMIZE TABLE
make plan7     # запит 7: план до і після індексу
make plan6     # запит 6: підзапит і LEFT JOIN
make equiv     # перевірка еквівалентності
```

`make show` виконує все підряд (близько 10 с). `make help` показує всі цілі. Цілі сторінок залежать одна від одної за порядком, починай з `make db`. Стовпці таблиці: `depth` рівні дерева, `level` 0 це листя, `pages`, `records`, `used_pct` заповненість, `frag_pct` зовнішня фрагментація.

Кроки сторінок 01-10 ідуть ланцюжком: `make random` сам виконує `db` і всі попередні кроки. `plan7`, `plan6`, `equiv` самі перестворюють базу.

## SQL по кроках

Кожен крок виконує файл із `sql/`. `make` друкує команду, клієнт `mysql -vvv` перед кожним результатом друкує сам запит. Після кроків 1-10 `pagestat.sh` друкує свої команди й SQL із `sql/stat-1-export.sql`, `stat-2-indexes.sql`, `stat-3-usage.sql` (копія `.ibd`, список індексів, обсяги); сторінки рахує `ibdstat.py`.

- `make db`: `northwind_opt.sql`, потім `sql/00-setup-procedures.sql`, процедури-цикли.
- `make empty`: `sql/01-empty-table.sql`, `CREATE TABLE` без ключа.
- `make one`: `sql/02-one-row.sql`, один `INSERT`.
- `make rows30`: `sql/03-rows30.sql`, `CALL fill_seq(2, 30)`.
- `make newpage`: `sql/04-new-page.sql`, `CALL probe_next_page()`.
- `make rows240`: `sql/05-rows240.sql`, дозаповнення до 240 рядків.
- `make pk`: `sql/06-primary-key.sql`, `ADD PRIMARY KEY (id)` і 18630 рядків.
- `make depth3`: `sql/07-depth3.sql`, 66138 рядків через рекурсивний CTE.
- `make seq`: `sql/08-sequential-key.sql`, ключ `pole1`, послідовні значення.
- `make random`: `sql/09-random-key.sql`, ключ `pole1` з `RANDOM_BYTES`.
- `make optimize`: `sql/10-optimize.sql`, `OPTIMIZE TABLE`; до: понад 200 сторінок із фрагментацією, після: 164 сторінки.
- `make plan7`: `sql/11-plan7-index.sql`; змінено: `CREATE INDEX idx_suppliers_country ON suppliers (Country, CompanyName)`, запит той самий.
- `make plan6`: `sql/12-plan6-rewrite.sql`; змінено: скалярний підзапит у SELECT замінено на `LEFT JOIN employees`.
- `make equiv`: `sql/13-equivalence.sql`, кількість рядків і `EXCEPT` в обидва боки для запитів 5 і 6.

## Що показати

1. `make empty`. `GEN_CLUST_INDEX`, `pages 1`, `records 0`, `used_pct 0.81`, `reserved_kb 16`. Таблиця без ключа в InnoDB кластеризується за прихованим ключем, порожня вже займає сторінку 16 КБ.
2. `make one`. `records 1`, `used_pct 2.51`.
3. `make rows30`. `records 30`, `used_pct 52.15`, одна сторінка.
4. `make newpage`. `new_page_row 55`, `pages_after 3`, дерево має два рівні: 2 листові сторінки й корінь. Сторінка 16 КБ, тому поріг 55-й рядок, а не 31-й, як у SQL Server.
5. `make rows240`. 5 листових сторінок і корінь, `used_pct 82.97`.
6. `make pk`. `PRIMARY`, 340 листових сторінок, `used_pct 92.59`, `depth 2`. Послідовний ключ заповнює сторінки майже повністю.
7. `make depth3`. 66138 рядків: `depth 3`, 1204 листові сторінки. Корінь вміщує близько 1200 посилань, далі потрібен третій рівень.
8. `make seq`. 165 сторінок, `used_pct 92.17`, `frag_pct 0.61`. Крок починається з `DROP TABLE IF EXISTS` для `test_index_nu` і `test_index_u`, яких ще немає, тому помилок немає.
9. `make random`. Близько 218-230 сторінок (щоразу трохи інакше), `used_pct` близько 67, `frag_pct` близько 99.5. Випадковий ключ дає внутрішню й зовнішню фрагментацію.
10. `make optimize`. `Table does not support optimize, doing recreate + analyze instead`, далі 164 сторінки, `used_pct 92.73`, `frag_pct 4.88`. Перебудова прибирає фрагментацію.
11. `make plan7`. До індексу `type ALL`, `rows 29`, `Using where`. Після `CREATE INDEX idx_suppliers_country`: `type ref`, `key idx_suppliers_country`, `rows 3`, `Using index`. Покриваючий індекс (Country, CompanyName) замінив повний перегляд.
12. `make plan6`. Підзапит: `Table scan on o` і `Select #2 (subquery in projection; dependent)` з `loops=830`. Після заміни: `Left hash join (e.EmployeeID = o.EmployeeID)` і `loops=1`. Залежний підзапит виконувався 830 разів, JOIN читає обидві таблиці по одному разу.
13. `make equiv`. Для запитів 5 і 6 однакові рядки (89 і 830), `EXCEPT` в обидва боки дає 0. Переписані запити дають той самий результат.
14. Час до і після (запит 6 це `0,648` і `0,181 мс`, мінімум за 500 запусків) лежить у звіті `report.pdf`, рис. 32, у цьому пайплайні його не перераховують.

Код: `sql/` (по кроках), подані `part1_test_index.sql` (кроки 00-19) і `part2_queries.sql` (запити, індекси, перевірка), `ibdstat.py` (читає сторінки `.ibd`), `pagestat.sh` (статистика з друком SQL). Подані `step.sh` і `stat.sh` роблять те саме без друку SQL.

## Звідки що береться

`make` друкує кожну команду завантаження бази, наприклад `./mysql.sh < schema.sql`. `mysql.sh` запускає клієнт `mysql` у контейнері `duikt-mysql`, `mysql-up.sh` піднімає цей контейнер.

- `northwind_opt.sql`: база `northwind_opt`, Northwind викладачки (дамп `MY_SQL Labwork2.sql` з Classroom, ПР 1.2) і два виправлення в кінці файла: знижки з `LabWork2.sql` і `AUTO_INCREMENT` для `products`.

`tidy.sh` лише скорочує вивід `mysql -vvv`: коментар із файла друкується одним рядком, запит після `mysql>`.

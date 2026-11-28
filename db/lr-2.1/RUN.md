# ЛР 2.1: запуск і показ

Потрібен лише Docker. Усі команди з теки `homework/db/lr-2.1`. Працює на окремій копії `northwind_defense`.

## Пайплайн

```sh
cd /home/skalse/job/duikt/course_3/students/lavrishko_vladyslav/homework/db/lr-2.1
make db         # свіжа копія northwind_defense
make proc       # процедура ShipProduct
make savepoint  # SAVEPOINT
make trigger    # тригер на UPDATE
make func       # функція OrderTotal
make view       # оновлюваність view
make role       # роль і користувач
```

`make defense` виконує шість останніх цілей підряд, `make show` виконує `db` і `defense`. `make help` показує всі цілі. Кроки змінюють базу, тому повторний запуск без `make db` дасть помилку (процедура, таблиця чи view вже є).

## SQL по кроках

Кожен крок виконує файл із `sql/`. `make` друкує команду, клієнт `mysql -vvv` перед кожним результатом друкує сам запит.

- `make proc`: `sql/01-ship-product.sql`, процедура `ShipProduct` на SERIALIZABLE, два виклики.
- `make savepoint`: `sql/02-savepoint.sql`, відкат до точки збереження.
- `make trigger`: `sql/03-update-trigger.sql`, `ProductsUpdateLog` і тригер AFTER UPDATE.
- `make func`: `sql/04-function.sql`, функція `OrderTotal` усередині SELECT.
- `make view`: `sql/05-view-updatable.sql`, простий view оновлюється, view з GROUP BY дає ERROR 1288.
- `make role`: `sql/06-role.sql`, роль `nw_reader`, користувач, `SHOW GRANTS`, видалення.

## Що показати

1. `make proc`, процедура `ShipProduct` (`SERIALIZABLE`): `UnitsInStock 39`, `Success`, `Failed`, `UnitsInStock 35`. Списання 4 одиниць пройшло, 100000 відхилено, залишок міняється лише при успіху.
2. `make savepoint`: `ALFKI | Kyiv | Germany`. Місто лишилось, країна відкотилась до точки збереження.
3. `make trigger`, тригер на UPDATE: журнал `1 | 2 | 19.0000 | 20.0000`. Старе й нове значення ціни записані автоматично.
4. `make func`, функція `OrderTotal`: замовлення `10248 | 440.00`.
5. `make view`, оновлюваність view: `ERROR 1288 ... v_city_counts ... is not updatable`. View із `GROUP BY` не оновлюється.
6. `make role`, роль: `SHOW GRANTS` для `lavrishko_ro` показує `GRANT SELECT` на базу `northwind_defense` і рядок з роллю `nw_reader`. Користувач і роль у кінці файла видаляються.

Код у `sql/` (по кроках) і в поданому файлі `defense_queries.sql`.

## Звідки що береться

`make` друкує кожну команду завантаження бази, наприклад `./mysql.sh < schema.sql`. `mysql.sh` запускає клієнт `mysql` у контейнері `duikt-mysql`, `mysql-up.sh` піднімає цей контейнер.

- `northwind_defense.sql`: база `northwind_defense`, Northwind викладачки (дамп `MY_SQL Labwork2.sql` з Classroom, ПР 1.2) і два виправлення в кінці файла: знижки з `LabWork2.sql` і `AUTO_INCREMENT` для `products`.

`tidy.sh` лише скорочує вивід `mysql -vvv`: коментар із файла друкується одним рядком, запит після `mysql>`.

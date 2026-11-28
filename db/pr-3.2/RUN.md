# ПР 3.2 і ЛР 3.2: запуск і показ

Потрібен лише Docker. Усі команди з теки `homework/db/pr-3.2`.

## Пайплайн

```sh
cd /home/skalse/job/duikt/course_3/students/lavrishko_vladyslav/homework/db/pr-3.2
make db         # свіжа база car_rental
make queries    # запити 6, 7, 8 через Jdbi
make crud       # додати, змінити, видалити авто
make errors     # помилки бази без падіння програми
make run        # меню вручну
```

`make show` виконує все підряд. `make help` показує всі цілі.

## SQL по кроках

- `make queries`: `sql/01-query6-active-rentals.sql`, `sql/02-query7-free-cars.sql`, `sql/03-query8-branch-revenue.sql`, три запити з `ReportDao.java` (іменовані параметри `:city`, `:maxPrice`).
- `make crud`: `sql/04-crud-insert-car.sql`, `sql/05-crud-update-car.sql`, `sql/06-crud-delete-car.sql`, SQL з анотацій `CarDao.java`.
- `make errors`: `sql/07-errors.sql`, запити, що викликають помилки 1062, 3819, 1452, 1451, і пояснення помилки вводу.
- `make run`: `sql/08-read-and-other-tables.sql`, читання й CRUD для `branches`, `customers`, `cars` при ручному вводі.
- `make sql`: друкує весь вміст `sql/`. `make show` перед кожним кроком друкує потрібні файли через `cat`.

## Що показати

1. `make queries`. Ті самі запити, що в ПР 3.1, але рядки мапляться в записи: заголовки в camelCase (`carId`, `plateNumber`, `dailyPrice`). Запит 7: Volkswagen Passat `1150.00`. Запит 8: Дніпро `102190.00`.
2. `make crud`. `Додано рядок, ключ: 25`, `Змінено рядків: 1`, `Видалено рядків: 1`.
3. `make errors`. `Помилка 1062` (UNIQUE), `Помилка 3819` (CHECK `chk_cars_price`), `Помилка 1452` і `Помилка 1451` (FOREIGN KEY), `Помилка вводу`.
4. `make run` за потреби: `1`, `3` дає 24 авто як записи `Car`.

Код: `src/main/java/` (записи `Car`, `BranchRevenue`, DAO `CarDao`, `ReportDao` на Jdbi), сценарії вводу в `demo/`, SQL кожного кроку в `sql/`.

## Звідки що береться

`make` друкує кожну команду завантаження бази, наприклад `./mysql.sh < schema.sql`. `mysql.sh` запускає клієнт `mysql` у контейнері `duikt-mysql`, `mysql-up.sh` піднімає цей контейнер.

- `schema.sql`, `seed.sql`: база `car_rental`, копія з ПР 1.1.

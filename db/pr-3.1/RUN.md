# ПР 3.1 і ЛР 3.1: запуск і показ

Потрібен лише Docker. Усі команди з теки `homework/db/pr-3.1`.

## Пайплайн

```sh
cd /home/skalse/job/duikt/course_3/students/lavrishko_vladyslav/homework/db/pr-3.1
make db         # свіжа база car_rental
make queries    # запити 6, 7, 8
make crud       # додати, змінити, видалити авто
make errors     # помилки бази без падіння програми
make run        # меню вручну
```

`make show` виконує все підряд. `make help` показує всі цілі.

## SQL по кроках

- `make queries`: `sql/01-query6-active-rentals.sql`, `sql/02-query7-free-cars.sql`, `sql/03-query8-branch-revenue.sql`, три запити з `QueryDao.java` (JOIN, параметри `?`, агрегація).
- `make crud`: `sql/04-crud-insert-car.sql`, `sql/05-crud-update-car.sql`, `sql/06-crud-delete-find-car.sql`, текст `INSERT`, `UPDATE`, `DELETE` і `SELECT`, який `TableDao.java` збирає для таблиці `cars`.
- `make errors`: `sql/07-errors.sql`, чотири запити, що викликають помилки 1062, 3819, 1451, 1364, і пояснення помилки вводу.
- `make run`: `sql/08-read-generic.sql`, показ таблиці й пошук за ключем при ручному вводі.
- `make sql`: друкує весь вміст `sql/`. `make show` перед кожним кроком друкує потрібні файли через `cat`.

## Що показати

1. `make queries`. Перший рядок `Підключено до БД: car_rental`. Запит 6 (JOIN): п'ять активних оренд, `Рядків: 5`. Запит 7 (параметри, місто `Київ`, ціна `1500`): Volkswagen Passat `1150.00`. Запит 8 (агрегація): Дніпро `102190.00`, Київ `60350.00`.
2. `make crud`. `Додано рядок, ключ: 25`, `Змінено рядків: 1`, `Видалено рядків: 1`, повторний пошук дає `Рядків: 0`.
3. `make errors`. Програма не падає: `Помилка 1062` (UNIQUE), `Помилка 3819` (CHECK), `Помилка 1451` (FOREIGN KEY, `fk_cars_branch ... ON DELETE RESTRICT`), `Помилка 1364` (NOT NULL), `Помилка вводу`.
4. `make run` за потреби: `1`, таблиця `1` дає 4 філії.

Код: `src/main/java/` (`Database`, `TableDao`, `QueryDao`, `Main`), сценарії вводу в `demo/`, SQL кожного кроку в `sql/`.

## Звідки що береться

`make` друкує кожну команду завантаження бази, наприклад `./mysql.sh < schema.sql`. `mysql.sh` запускає клієнт `mysql` у контейнері `duikt-mysql`, `mysql-up.sh` піднімає цей контейнер.

- `schema.sql`, `seed.sql`: база `car_rental`, копія з ПР 1.1.

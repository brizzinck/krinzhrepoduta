# ПР 1.1: запуск і показ

Потрібен лише Docker. Усі команди з теки `homework/db/pr-1.1`.

## Пайплайн

```sh
cd /home/skalse/job/duikt/course_3/students/lavrishko_vladyslav/homework/db/pr-1.1
make db         # бази car_rental і car_rental_check з нуля
make counts     # рядки в таблицях
make sample     # приклад даних
make ddl        # обмеження таблиці rentals
make check      # перевірка обмежень на копії
```

## SQL по кроках

- `make counts`: `sql/01-counts.sql`, кількість рядків у 10 таблицях.
- `make sample`: `sql/02-sample.sql`, п'ять авто.
- `make ddl`: `sql/03-ddl.sql`, `SHOW CREATE TABLE rentals` (у виводі лишаються рядки `CONSTRAINT`).
- `make check`: `sql/04-check.sql`, перевірка обмежень на копії `car_rental_check` (помилки очікувані, тому `--force`).

`make show` виконує все підряд. `make help` показує всі цілі. Моделі (рис. 1 і 2) відкрити в `report.pdf`.

## Що показати

1. `make counts`. Таблиця: `branches 4`, `car_classes 5`, `car_models 12`, `cars 24`, `customers 20`, `employees 8`, `rentals 30`, `payments 81`, `extras 5`, `rental_extras 21`. Це 10 таблиць даталогічної моделі з даними.
2. `make sample`. Перший рядок `VF17LZZ6ETYVF057J | AA8367AT | 450.00 | retired`. Дані завантажено за `seed.sql`.
3. `make ddl`. Рядки `fk_rentals_employee ... ON DELETE SET NULL`, `chk_rentals_planned`, `chk_rentals_status`. Обмеження іменовані й мають різні дії `ON DELETE`.
4. `make check`. Помилки за порядком: три `ERROR 3819` (`chk_cars_price`, `chk_cars_status`, `chk_rentals_planned`), `ERROR 1062` (дубль VIN), `ERROR 1452` (неіснуюча модель), `ERROR 1451` (філіал з авто, `RESTRICT`). Далі `extras_before_delete 2`, `extras_after_delete 0` (`CASCADE`), `rentals_without_employee_before 5`, `rentals_without_employee_after 6` (`SET NULL`). Останній `ERROR 1451` для `SET DEFAULT`: InnoDB цю дію не виконує. Усі обмеження працюють.

`make check` спершу пересоздає бази, тому повторюється без ручного скидання.

Код: `schema.sql` (таблиці й обмеження), `seed.sql` (дані), `sql/` (SQL кожного кроку показу), `check.sql` (перевірка обмежень, лишається як у звіті).

## Звідки що береться

`make` друкує кожну команду, наприклад `./mysql.sh -t -vvv car_rental < sql/01-counts.sql`; `-vvv` друкує кожен запит перед результатом. `mysql.sh` запускає клієнт `mysql` у контейнері `duikt-mysql`, `mysql-up.sh` піднімає цей контейнер.

- `car_rental_check.sql`: копія `car_rental` для перевірки обмежень.

`tidy.sh` лише скорочує вивід `mysql -vvv`: коментар із файла друкується одним рядком, запит після `mysql>`.

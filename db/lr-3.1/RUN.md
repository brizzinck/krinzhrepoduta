# ЛР 3.1: запуск і показ

Потрібен лише Docker. Програма `ConnectDemo.java` бере драйвер MySQL із jar ПР 3.1.

## Пайплайн

```sh
cd /home/skalse/job/duikt/course_3/students/lavrishko_vladyslav/homework/db/lr-3.1
make show       # свіжа база car_rental, jar ПР 3.1, запуск ConnectDemo
```

## SQL по кроках

- `make show`: `sql/01-insert-extra.sql`, `sql/02-select-extras.sql`, `sql/03-delete-extra.sql`, три запити з `ConnectDemo.java` (вставка, читання, видалення).
- `make sql`: друкує весь вміст `sql/`.

## Що показати

1. `make show`. `З'єднання відкрито: car_rental`, `Додано рядків: 1`, шість послуг (остання `6 | Демо-послуга | 15.00`), `Видалено рядків: 1`. Тестовий рядок програма видаляє сама, база лишається чистою.

Код: `ConnectDemo.java`, SQL кожного кроку в `sql/`.

## Звідки що береться

`make` друкує кожну команду завантаження бази, наприклад `./mysql.sh < schema.sql`. `mysql.sh` запускає клієнт `mysql` у контейнері `duikt-mysql`, `mysql-up.sh` піднімає цей контейнер.

- `schema.sql`, `seed.sql`: база `car_rental`, копія з ПР 1.1.
- `pr-3.1/`: копія ПР 3.1 із зібраним jar.

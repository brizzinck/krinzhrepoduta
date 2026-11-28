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

`make show` виконує все підряд. `make help` показує всі цілі. ЛР 3.1: `cd ../lr-3.1 && make show`.

## Що показати

1. `make queries`. Перший рядок `Підключено до БД: car_rental`. Запит 6 (JOIN): п'ять активних оренд, `Рядків: 5`. Запит 7 (параметри, місто `Київ`, ціна `1500`): Volkswagen Passat `1150.00`. Запит 8 (агрегація): Дніпро `102190.00`, Київ `60350.00`.
2. `make crud`. `Додано рядок, ключ: 25`, `Змінено рядків: 1`, `Видалено рядків: 1`, повторний пошук дає `Рядків: 0`.
3. `make errors`. Програма не падає: `Помилка 1062` (UNIQUE), `Помилка 3819` (CHECK), `Помилка 1451` (FOREIGN KEY, `fk_cars_branch ... ON DELETE RESTRICT`), `Помилка 1364` (NOT NULL), `Помилка вводу`.
4. `make run` за потреби: `1`, таблиця `1` дає 4 філії.

Код: `src/main/java/` (`Database`, `TableDao`, `QueryDao`, `Main`), сценарії вводу в `demo/`.

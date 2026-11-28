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

## Що показати

1. `make queries`. Ті самі запити, що в ПР 3.1, але рядки мапляться в записи: заголовки в camelCase (`carId`, `plateNumber`, `dailyPrice`). Запит 7: Volkswagen Passat `1150.00`. Запит 8: Дніпро `102190.00`.
2. `make crud`. `Додано рядок, ключ: 25`, `Змінено рядків: 1`, `Видалено рядків: 1`.
3. `make errors`. `Помилка 1062` (UNIQUE), `Помилка 3819` (CHECK `chk_cars_price`), `Помилка 1452` і `Помилка 1451` (FOREIGN KEY), `Помилка вводу`.
4. `make run` за потреби: `1`, `3` дає 24 авто як записи `Car`.

Код: `src/main/java/` (записи `Car`, `BranchRevenue`, DAO `CarDao`, `ReportDao` на Jdbi), сценарії вводу в `demo/`.

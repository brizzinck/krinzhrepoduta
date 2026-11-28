-- Додати авто: меню пункт 3, таблиця 4 (cars).
-- Звідки: src/main/java/rental/TableDao.java, метод insert(Table, Map). SQL збирається з імен
-- стовпців лише заповнених полів: "INSERT INTO " + table + " (" + columns + ") VALUES (" + marks + ")".
-- Нижче готовий текст для сценарію demo/02-create.txt. Порожні поля (mileage_km, status) пропущені,
-- для них діють DEFAULT. Значення (setString): 1, 1, 'WVWZZZ1KZDP123456', 'AA0001TT', 2024, 1500.00.
-- Ключ car_id генерує MySQL (RETURN_GENERATED_KEYS), у демо це 25.
INSERT INTO cars (model_id, branch_id, vin, plate_number, manufacture_year, daily_price)
VALUES (?, ?, ?, ?, ?, ?);

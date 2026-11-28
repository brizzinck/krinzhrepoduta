-- Змінити авто: меню пункт 4, таблиця 4 (cars), ключ 25.
-- Звідки: src/main/java/rental/TableDao.java, метод update(Table, List, Map). SQL збирається так:
-- "UPDATE " + table + " SET " + (column = ?, ...) + " WHERE " + table.keyCondition().
-- Нижче готовий текст для сценарію demo/03-update.txt. Порожні поля не змінюються й до SQL не потрапляють.
-- Значення (setString): '12500', '1350.50', 'maintenance'; ключ (setInt): 25.
UPDATE cars SET mileage_km = ?, daily_price = ?, status = ? WHERE car_id = ?;

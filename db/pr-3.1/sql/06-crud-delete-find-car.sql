-- Видалити авто і перевірити: меню пункти 5 і 2, таблиця 4 (cars), ключ 25.
-- Звідки: src/main/java/rental/TableDao.java, методи delete(Table, List) і findById(Table, List).
-- SQL збирається з імені таблиці й умови table.keyCondition(). Сценарій: demo/04-delete.txt.
-- Ключ (setInt): 25 в обох запитах. Після DELETE повторний пошук дає 0 рядків.
DELETE FROM cars WHERE car_id = ?;
SELECT * FROM cars WHERE car_id = ?;

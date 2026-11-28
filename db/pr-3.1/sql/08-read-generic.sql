-- Показати таблицю і знайти за ключем: меню пункти 1 і 2 (ручний показ, у make show не входить).
-- Звідки: src/main/java/rental/TableDao.java, методи findAll(Table) і findById(Table, List).
-- SQL збирається з імені таблиці й ключових стовпців (Table.java). Нижче текст для сценарію demo/01-read.txt:
-- пункт 1 таблиця 1 (branches) і пункт 2 таблиця 4 (cars), ключ 2 (setInt).
SELECT * FROM branches ORDER BY branch_id;
SELECT * FROM cars WHERE car_id = ?;

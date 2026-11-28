-- Крок 3 ConnectDemo: видалення тестового рядка. Звідки: ConnectDemo.java, метод main, третій PreparedStatement (delete).
-- Значення: ? = 'Демо-послуга' (setString). Програма друкує "Видалено рядків: 1", база лишається чистою.
DELETE FROM extras WHERE name = ?;

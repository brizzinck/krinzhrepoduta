-- Крок 1 ConnectDemo: вставка рядка. Звідки: ConnectDemo.java, метод main, перший PreparedStatement (insert).
-- Значення: перший ? = 'Демо-послуга' (setString), другий ? = 15.00 (setBigDecimal).
-- Програма друкує "Додано рядків: 1" (результат executeUpdate).
INSERT INTO extras (name, price_per_day) VALUES (?, ?);

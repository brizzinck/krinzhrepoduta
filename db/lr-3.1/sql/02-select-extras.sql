-- Крок 2 ConnectDemo: читання через ResultSet. Звідки: ConnectDemo.java, метод main, другий PreparedStatement (select).
-- Параметрів немає. Програма друкує кожен рядок як "extra_id | name | price_per_day", останній 6 | Демо-послуга | 15.00.
SELECT extra_id, name, price_per_day FROM extras ORDER BY extra_id;

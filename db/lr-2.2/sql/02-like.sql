-- Крок 2. LIKE: початок рядка використовує індекс, шаблон із % на початку ні.
-- Підготовка: індекс по назві компанії, потім свіжа статистика.
CREATE INDEX idx_customers_name ON customers (CompanyName);
ANALYZE TABLE customers;

-- До: шаблон із % на початку, повний перегляд (type ALL, rows 91).
EXPLAIN SELECT ContactName FROM customers WHERE CompanyName LIKE '%a';

-- Зміна: шаблон із відомим початком 'A%' замість '%a'.
-- Після: індекс використовується (type range, rows 4).
EXPLAIN SELECT CompanyName FROM customers WHERE CompanyName LIKE 'A%';

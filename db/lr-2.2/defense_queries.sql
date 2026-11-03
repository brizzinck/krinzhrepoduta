-- ЛР 2.2. Додаткові приклади до ПР 2.2 (робота на копії бази northwind_defense).
USE northwind_defense;

-- 1. Функція над стовпцем заважає індексу, діапазон ні
CREATE INDEX idx_orders_date ON orders (OrderDate);
ANALYZE TABLE orders;
EXPLAIN SELECT OrderID FROM orders WHERE YEAR(OrderDate) = 1997;
EXPLAIN SELECT OrderID FROM orders WHERE OrderDate >= '1997-01-01' AND OrderDate < '1998-01-01';

-- 2. LIKE: початок рядка використовує індекс, '%...' ні
CREATE INDEX idx_customers_name ON customers (CompanyName);
ANALYZE TABLE customers;
EXPLAIN SELECT CompanyName FROM customers WHERE CompanyName LIKE 'A%';
EXPLAIN SELECT ContactName FROM customers WHERE CompanyName LIKE '%a';

-- 3. Малоселективний стовпець: при Discontinued = 0 індекс майже нічого не відсіює (69 рядків із 77)
CREATE INDEX idx_products_disc ON products (Discontinued);
ANALYZE TABLE products;
SELECT Discontinued, COUNT(*) AS cnt FROM products GROUP BY Discontinued;
EXPLAIN SELECT ProductName FROM products WHERE Discontinued = 0;
EXPLAIN SELECT ProductName FROM products WHERE Discontinued = 1;

-- 4. Невидимий індекс: перевірка, що буде без нього, без видалення
ALTER TABLE orders ALTER INDEX idx_orders_date INVISIBLE;
EXPLAIN SELECT OrderID FROM orders WHERE OrderDate >= '1997-01-01' AND OrderDate < '1997-02-01';
ALTER TABLE orders ALTER INDEX idx_orders_date VISIBLE;
EXPLAIN SELECT OrderID FROM orders WHERE OrderDate >= '1997-01-01' AND OrderDate < '1997-02-01';

-- 5. DISTINCT після JOIN замінюється на EXISTS (слайд 39 лекції 3)
EXPLAIN SELECT DISTINCT c.CustomerID FROM customers c JOIN orders o ON o.CustomerID = c.CustomerID;
EXPLAIN SELECT c.CustomerID FROM customers c WHERE EXISTS (SELECT 1 FROM orders o WHERE o.CustomerID = c.CustomerID);

-- ПР 2.2, завдання 2-5: запити 3-7 варіанта 2 до оптимізації і після неї (MySQL 8.4, база northwind_opt).
-- Порядок: ../_shared/northwind/setup_northwind.sh northwind_opt; ./step.sh analyze part2_queries.sql;
-- ./qstat.sh q3 before (так само q4..q7); ./step.sh indexes part2_queries.sql;
-- ./qstat.sh q3 after, q4 after, q5_new after, q6_new after, q7 after; ./step.sh equivalence part2_queries.sql.
-- Скрипт qstat.sh виконує для запиту EXPLAIN, EXPLAIN ANALYZE, EXPLAIN FORMAT=JSON, крок handler і 500 запусків із кроком digest.
USE northwind_opt;

-- analyze. Статистика оптимізатора перед станом "до": без неї query_cost на свіжій базі нестабільний
ANALYZE TABLE products, customers, employees, orders, `order details`, suppliers, categories;

-- q3. Запит 3
SELECT c.CategoryName, ROUND(AVG(p.UnitPrice), 2) AS avg_price
FROM products p
JOIN categories c ON c.CategoryID = p.CategoryID
GROUP BY c.CategoryID, c.CategoryName
ORDER BY c.CategoryName;

-- q4. Запит 4
SELECT e.City,
       COUNT(DISTINCT e.EmployeeID) AS employees,
       COUNT(DISTINCT c.CustomerID) AS customers
FROM employees e
LEFT JOIN customers c ON c.City = e.City
GROUP BY e.City
ORDER BY e.City;

-- q5. Запит 5
SELECT c.CustomerID, c.CompanyName,
       ROUND(SUM(od.UnitPrice * od.Quantity * (1 - od.Discount)), 2) AS revenue
FROM customers c
JOIN orders o ON o.CustomerID = c.CustomerID
JOIN `order details` od ON od.OrderID = o.OrderID
GROUP BY c.CustomerID, c.CompanyName
ORDER BY revenue DESC;

-- q6. Запит 6
SELECT o.OrderID, o.OrderDate, o.CustomerID,
       (SELECT CONCAT(e.FirstName, ' ', e.LastName)
        FROM employees e
        WHERE e.EmployeeID = o.EmployeeID) AS employee
FROM orders o;

-- q7. Запит 7
SELECT CompanyName, 'Supplier' AS role FROM suppliers WHERE Country = 'Germany'
UNION ALL
SELECT CompanyName, 'Customer' FROM customers WHERE Country = 'Germany';

-- indexes. Оптимізація: індекси для запитів 3, 4 і 7
CREATE INDEX idx_products_cat_price ON products (CategoryID, UnitPrice);
CREATE INDEX idx_customers_city ON customers (City);
CREATE INDEX idx_employees_city ON employees (City);
CREATE INDEX idx_suppliers_country ON suppliers (Country, CompanyName);
CREATE INDEX idx_customers_country ON customers (Country, CompanyName);
ANALYZE TABLE products, customers, employees, suppliers;

-- q5_new. Запит 5 після оптимізації: групування лише за первинним ключем, CompanyName від нього залежить
SELECT c.CustomerID, c.CompanyName,
       ROUND(SUM(od.UnitPrice * od.Quantity * (1 - od.Discount)), 2) AS revenue
FROM customers c
JOIN orders o ON o.CustomerID = c.CustomerID
JOIN `order details` od ON od.OrderID = o.OrderID
GROUP BY c.CustomerID
ORDER BY revenue DESC;

-- q6_new. Запит 6 після оптимізації: скалярний підзапит замінено на з'єднання
SELECT o.OrderID, o.OrderDate, o.CustomerID, CONCAT(e.FirstName, ' ', e.LastName) AS employee
FROM orders o
LEFT JOIN employees e ON e.EmployeeID = o.EmployeeID;

-- equivalence. Переписані запити дають той самий результат
SELECT 'Q5: rows in original' AS check_name, COUNT(*) AS cnt FROM (SELECT c.CustomerID, c.CompanyName,
       ROUND(SUM(od.UnitPrice * od.Quantity * (1 - od.Discount)), 2) AS revenue
FROM customers c
JOIN orders o ON o.CustomerID = c.CustomerID
JOIN `order details` od ON od.OrderID = o.OrderID
GROUP BY c.CustomerID, c.CompanyName
ORDER BY revenue DESC) a
UNION ALL SELECT 'Q5: rows in rewritten', COUNT(*) FROM (SELECT c.CustomerID, c.CompanyName,
       ROUND(SUM(od.UnitPrice * od.Quantity * (1 - od.Discount)), 2) AS revenue
FROM customers c
JOIN orders o ON o.CustomerID = c.CustomerID
JOIN `order details` od ON od.OrderID = o.OrderID
GROUP BY c.CustomerID
ORDER BY revenue DESC) b
UNION ALL SELECT 'Q5: original EXCEPT rewritten', COUNT(*) FROM ((SELECT c.CustomerID, c.CompanyName,
       ROUND(SUM(od.UnitPrice * od.Quantity * (1 - od.Discount)), 2) AS revenue
FROM customers c
JOIN orders o ON o.CustomerID = c.CustomerID
JOIN `order details` od ON od.OrderID = o.OrderID
GROUP BY c.CustomerID, c.CompanyName
ORDER BY revenue DESC) EXCEPT (SELECT c.CustomerID, c.CompanyName,
       ROUND(SUM(od.UnitPrice * od.Quantity * (1 - od.Discount)), 2) AS revenue
FROM customers c
JOIN orders o ON o.CustomerID = c.CustomerID
JOIN `order details` od ON od.OrderID = o.OrderID
GROUP BY c.CustomerID
ORDER BY revenue DESC)) x
UNION ALL SELECT 'Q5: rewritten EXCEPT original', COUNT(*) FROM ((SELECT c.CustomerID, c.CompanyName,
       ROUND(SUM(od.UnitPrice * od.Quantity * (1 - od.Discount)), 2) AS revenue
FROM customers c
JOIN orders o ON o.CustomerID = c.CustomerID
JOIN `order details` od ON od.OrderID = o.OrderID
GROUP BY c.CustomerID
ORDER BY revenue DESC) EXCEPT (SELECT c.CustomerID, c.CompanyName,
       ROUND(SUM(od.UnitPrice * od.Quantity * (1 - od.Discount)), 2) AS revenue
FROM customers c
JOIN orders o ON o.CustomerID = c.CustomerID
JOIN `order details` od ON od.OrderID = o.OrderID
GROUP BY c.CustomerID, c.CompanyName
ORDER BY revenue DESC)) y
UNION ALL SELECT 'Q6: rows in original', COUNT(*) FROM (SELECT o.OrderID, o.OrderDate, o.CustomerID,
       (SELECT CONCAT(e.FirstName, ' ', e.LastName)
        FROM employees e
        WHERE e.EmployeeID = o.EmployeeID) AS employee
FROM orders o) a
UNION ALL SELECT 'Q6: rows in rewritten', COUNT(*) FROM (SELECT o.OrderID, o.OrderDate, o.CustomerID, CONCAT(e.FirstName, ' ', e.LastName) AS employee
FROM orders o
LEFT JOIN employees e ON e.EmployeeID = o.EmployeeID) b
UNION ALL SELECT 'Q6: original EXCEPT rewritten', COUNT(*) FROM ((SELECT o.OrderID, o.OrderDate, o.CustomerID,
       (SELECT CONCAT(e.FirstName, ' ', e.LastName)
        FROM employees e
        WHERE e.EmployeeID = o.EmployeeID) AS employee
FROM orders o) EXCEPT (SELECT o.OrderID, o.OrderDate, o.CustomerID, CONCAT(e.FirstName, ' ', e.LastName) AS employee
FROM orders o
LEFT JOIN employees e ON e.EmployeeID = o.EmployeeID)) x
UNION ALL SELECT 'Q6: rewritten EXCEPT original', COUNT(*) FROM ((SELECT o.OrderID, o.OrderDate, o.CustomerID, CONCAT(e.FirstName, ' ', e.LastName) AS employee
FROM orders o
LEFT JOIN employees e ON e.EmployeeID = o.EmployeeID) EXCEPT (SELECT o.OrderID, o.OrderDate, o.CustomerID,
       (SELECT CONCAT(e.FirstName, ' ', e.LastName)
        FROM employees e
        WHERE e.EmployeeID = o.EmployeeID) AS employee
FROM orders o)) y;

-- handler. Лічильники звернень до рядків за сеанс (перед запитом FLUSH STATUS)
SHOW SESSION STATUS WHERE Variable_name IN ('Handler_read_first', 'Handler_read_key', 'Handler_read_next', 'Handler_read_rnd_next');

-- digest. Час за performance_schema: запит, виконаний щонайменше 500 разів
SELECT COUNT_STAR AS runs, ROUND(AVG_TIMER_WAIT / 1e9, 3) AS avg_ms, ROUND(MIN_TIMER_WAIT / 1e9, 3) AS min_ms,
       ROUND(MAX_TIMER_WAIT / 1e9, 3) AS max_ms, ROUND(SUM_ROWS_EXAMINED / COUNT_STAR) AS rows_exam,
       ROUND(SUM_ROWS_SENT / COUNT_STAR) AS rows_sent, ROUND(SUM_CREATED_TMP_TABLES / COUNT_STAR) AS tmp_tables,
       ROUND(SUM_SORT_ROWS / COUNT_STAR) AS sort_rows
FROM performance_schema.events_statements_summary_by_digest
WHERE SCHEMA_NAME = 'northwind_opt' AND COUNT_STAR >= 500;

-- ПР 1.2, варіант 2. База northwind (MySQL 8.4), див. setup_northwind.sh.
-- Назви таблиць малими літерами (lower_case_table_names = 0 на Linux), `order details` у зворотних лапках.

-- Запит 1. Продукти, ціна яких більша за 20 доларів
SELECT ProductID, ProductName, UnitPrice
FROM products
WHERE UnitPrice > 20
ORDER BY UnitPrice DESC, ProductName;

-- Запит 2. Замовлення, зроблені у вівторок (DAYOFWEEK: неділя = 1, вівторок = 3)
SELECT OrderID, CustomerID, OrderDate, DAYNAME(OrderDate) AS day_name
FROM orders
WHERE DAYOFWEEK(OrderDate) = 3
ORDER BY OrderDate;

-- Запит 3. Середня ціна продуктів для кожної категорії
SELECT c.CategoryName, ROUND(AVG(p.UnitPrice), 2) AS avg_price
FROM products p
JOIN categories c ON c.CategoryID = p.CategoryID
GROUP BY c.CategoryID, c.CategoryName
ORDER BY c.CategoryName;

-- Запит 4 (JOIN). Кількість співробітників і клієнтів у кожному місті, де є співробітники
SELECT e.City,
       COUNT(DISTINCT e.EmployeeID) AS employees,
       COUNT(DISTINCT c.CustomerID) AS customers
FROM employees e
LEFT JOIN customers c ON c.City = e.City
GROUP BY e.City
ORDER BY e.City;

-- Запит 5 (JOIN). Доходи від кожного клієнта (з урахуванням знижки)
SELECT c.CustomerID, c.CompanyName,
       ROUND(SUM(od.UnitPrice * od.Quantity * (1 - od.Discount)), 2) AS revenue
FROM customers c
JOIN orders o ON o.CustomerID = c.CustomerID
JOIN `order details` od ON od.OrderID = o.OrderID
GROUP BY c.CustomerID, c.CompanyName
ORDER BY revenue DESC;

-- Запит 6 (підзапити). Замовлення та відповідні співробітники
SELECT o.OrderID, o.OrderDate, o.CustomerID,
       (SELECT CONCAT(e.FirstName, ' ', e.LastName)
        FROM employees e
        WHERE e.EmployeeID = o.EmployeeID) AS employee
FROM orders o;

-- Запит 7 (множинна операція). Постачальники й клієнти з Німеччини без видалення дублікатів
SELECT CompanyName, 'Supplier' AS role FROM suppliers WHERE Country = 'Germany'
UNION ALL
SELECT CompanyName, 'Customer' FROM customers WHERE Country = 'Germany';

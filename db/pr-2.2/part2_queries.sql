-- ПР 2.2, частина 2: запити 3-7 варіанта 2 до оптимізації і після неї. База northwind_opt, MySQL 8.4.
USE northwind_opt;

-- Запит 3, до оптимізації
EXPLAIN SELECT c.CategoryName, ROUND(AVG(p.UnitPrice), 2) AS avg_price
FROM products p
JOIN categories c ON c.CategoryID = p.CategoryID
GROUP BY c.CategoryID, c.CategoryName
ORDER BY c.CategoryName;
EXPLAIN ANALYZE SELECT c.CategoryName, ROUND(AVG(p.UnitPrice), 2) AS avg_price
FROM products p
JOIN categories c ON c.CategoryID = p.CategoryID
GROUP BY c.CategoryID, c.CategoryName
ORDER BY c.CategoryName;

-- Запит 4, до оптимізації
EXPLAIN SELECT e.City,
       COUNT(DISTINCT e.EmployeeID) AS employees,
       COUNT(DISTINCT c.CustomerID) AS customers
FROM employees e
LEFT JOIN customers c ON c.City = e.City
GROUP BY e.City
ORDER BY e.City;
EXPLAIN ANALYZE SELECT e.City,
       COUNT(DISTINCT e.EmployeeID) AS employees,
       COUNT(DISTINCT c.CustomerID) AS customers
FROM employees e
LEFT JOIN customers c ON c.City = e.City
GROUP BY e.City
ORDER BY e.City;

-- Запит 5, до оптимізації
EXPLAIN SELECT c.CustomerID, c.CompanyName,
       ROUND(SUM(od.UnitPrice * od.Quantity * (1 - od.Discount)), 2) AS revenue
FROM customers c
JOIN orders o ON o.CustomerID = c.CustomerID
JOIN `order details` od ON od.OrderID = o.OrderID
GROUP BY c.CustomerID, c.CompanyName
ORDER BY revenue DESC;
EXPLAIN ANALYZE SELECT c.CustomerID, c.CompanyName,
       ROUND(SUM(od.UnitPrice * od.Quantity * (1 - od.Discount)), 2) AS revenue
FROM customers c
JOIN orders o ON o.CustomerID = c.CustomerID
JOIN `order details` od ON od.OrderID = o.OrderID
GROUP BY c.CustomerID, c.CompanyName
ORDER BY revenue DESC;

-- Запит 6, до оптимізації
EXPLAIN SELECT o.OrderID, o.OrderDate, o.CustomerID,
       (SELECT CONCAT(e.FirstName, ' ', e.LastName)
        FROM employees e
        WHERE e.EmployeeID = o.EmployeeID) AS employee
FROM orders o;
EXPLAIN ANALYZE SELECT o.OrderID, o.OrderDate, o.CustomerID,
       (SELECT CONCAT(e.FirstName, ' ', e.LastName)
        FROM employees e
        WHERE e.EmployeeID = o.EmployeeID) AS employee
FROM orders o;

-- Запит 7, до оптимізації
EXPLAIN SELECT CompanyName, 'Supplier' AS role FROM suppliers WHERE Country = 'Germany'
UNION ALL
SELECT CompanyName, 'Customer' FROM customers WHERE Country = 'Germany';
EXPLAIN ANALYZE SELECT CompanyName, 'Supplier' AS role FROM suppliers WHERE Country = 'Germany'
UNION ALL
SELECT CompanyName, 'Customer' FROM customers WHERE Country = 'Germany';

-- Оптимізація: індекси
CREATE INDEX idx_products_cat_price ON products (CategoryID, UnitPrice);
CREATE INDEX idx_customers_city ON customers (City);
CREATE INDEX idx_employees_city ON employees (City);
CREATE INDEX idx_suppliers_country ON suppliers (Country, CompanyName);
CREATE INDEX idx_customers_country ON customers (Country, CompanyName);
ANALYZE TABLE products;
ANALYZE TABLE customers;
ANALYZE TABLE employees;
ANALYZE TABLE suppliers;

-- Запит 3, після оптимізації
EXPLAIN SELECT c.CategoryName, ROUND(AVG(p.UnitPrice), 2) AS avg_price
FROM products p
JOIN categories c ON c.CategoryID = p.CategoryID
GROUP BY c.CategoryID, c.CategoryName
ORDER BY c.CategoryName;
EXPLAIN ANALYZE SELECT c.CategoryName, ROUND(AVG(p.UnitPrice), 2) AS avg_price
FROM products p
JOIN categories c ON c.CategoryID = p.CategoryID
GROUP BY c.CategoryID, c.CategoryName
ORDER BY c.CategoryName;

-- Запит 4, після оптимізації
EXPLAIN SELECT e.City,
       COUNT(DISTINCT e.EmployeeID) AS employees,
       COUNT(DISTINCT c.CustomerID) AS customers
FROM employees e
LEFT JOIN customers c ON c.City = e.City
GROUP BY e.City
ORDER BY e.City;
EXPLAIN ANALYZE SELECT e.City,
       COUNT(DISTINCT e.EmployeeID) AS employees,
       COUNT(DISTINCT c.CustomerID) AS customers
FROM employees e
LEFT JOIN customers c ON c.City = e.City
GROUP BY e.City
ORDER BY e.City;

-- Запит 5, після оптимізації
EXPLAIN SELECT c.CustomerID, c.CompanyName,
       ROUND(SUM(od.UnitPrice * od.Quantity * (1 - od.Discount)), 2) AS revenue
FROM customers c
JOIN orders o ON o.CustomerID = c.CustomerID
JOIN `order details` od ON od.OrderID = o.OrderID
GROUP BY c.CustomerID
ORDER BY revenue DESC;
EXPLAIN ANALYZE SELECT c.CustomerID, c.CompanyName,
       ROUND(SUM(od.UnitPrice * od.Quantity * (1 - od.Discount)), 2) AS revenue
FROM customers c
JOIN orders o ON o.CustomerID = c.CustomerID
JOIN `order details` od ON od.OrderID = o.OrderID
GROUP BY c.CustomerID
ORDER BY revenue DESC;

-- Запит 6, після оптимізації
EXPLAIN SELECT o.OrderID, o.OrderDate, o.CustomerID, CONCAT(e.FirstName, ' ', e.LastName) AS employee
FROM orders o
LEFT JOIN employees e ON e.EmployeeID = o.EmployeeID;
EXPLAIN ANALYZE SELECT o.OrderID, o.OrderDate, o.CustomerID, CONCAT(e.FirstName, ' ', e.LastName) AS employee
FROM orders o
LEFT JOIN employees e ON e.EmployeeID = o.EmployeeID;

-- Запит 7, після оптимізації
EXPLAIN SELECT CompanyName, 'Supplier' AS role FROM suppliers WHERE Country = 'Germany'
UNION ALL
SELECT CompanyName, 'Customer' FROM customers WHERE Country = 'Germany';
EXPLAIN ANALYZE SELECT CompanyName, 'Supplier' AS role FROM suppliers WHERE Country = 'Germany'
UNION ALL
SELECT CompanyName, 'Customer' FROM customers WHERE Country = 'Germany';

-- 1. DISTINCT, ORDER BY, NULL (слайди 5-19)
SELECT DISTINCT Country FROM customers ORDER BY Country LIMIT 5;
SELECT EmployeeID, LastName, Region FROM employees ORDER BY Region, LastName;
SELECT COUNT(*) AS all_rows, COUNT(Region) AS not_null_region, COUNT(*) - COUNT(Region) AS null_region FROM customers;
SELECT CustomerID, CompanyName, COALESCE(Region, 'n/a') AS region FROM customers ORDER BY CustomerID LIMIT 5;
-- 2. WHERE: діапазон, LIKE, IN, IS NULL (слайди 20-28)
SELECT ProductID, ProductName, UnitPrice FROM products WHERE UnitPrice BETWEEN 10 AND 12 ORDER BY UnitPrice, ProductID;
SELECT CompanyName FROM customers WHERE CompanyName LIKE 'A%' ORDER BY CompanyName;
SELECT CompanyName FROM customers WHERE CompanyName LIKE '__C%' ORDER BY CompanyName;
SELECT CustomerID, Country FROM customers WHERE Country IN ('UK', 'Spain') ORDER BY Country, CustomerID LIMIT 6;
SELECT COUNT(*) AS customers_without_region FROM customers WHERE Region IS NULL;
-- 3. Функції (слайди 30-37)
SELECT UPPER(LEFT(ProductName, 5)) AS up5, LENGTH(ProductName) AS len, ROUND(UnitPrice * 1.2, 1) AS price_vat, CAST(UnitPrice AS SIGNED) AS price_int FROM products ORDER BY ProductID LIMIT 3;
SELECT OrderID, OrderDate, YEAR(OrderDate) AS y, MONTH(OrderDate) AS m, DATE_FORMAT(OrderDate, '%d.%m.%Y') AS d, DATEDIFF(ShippedDate, OrderDate) AS days_to_ship
FROM orders ORDER BY OrderID LIMIT 4;

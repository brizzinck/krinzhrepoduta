SELECT COLUMN_TYPE AS discount_type FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'northwind' AND TABLE_NAME = 'order details' AND COLUMN_NAME = 'Discount';
SELECT COUNT(*) AS rows_total, SUM(Discount <> 0) AS rows_with_discount, MAX(Discount) AS max_discount
FROM `order details`;
SELECT Discount, COUNT(*) AS rows_cnt FROM `order details` GROUP BY Discount ORDER BY Discount;
SELECT ROUND(SUM(od.UnitPrice * od.Quantity * (1 - od.Discount)), 4) AS revenue_1997
FROM orders o JOIN `order details` od ON od.OrderID = o.OrderID
WHERE YEAR(o.OrderDate) = 1997;
SELECT c.CompanyName, ROUND(SUM(od.UnitPrice * od.Quantity * (1 - od.Discount)), 3) AS revenue
FROM customers c JOIN orders o ON o.CustomerID = c.CustomerID JOIN `order details` od ON od.OrderID = o.OrderID
WHERE c.CompanyName = 'QUICK-Stop' GROUP BY c.CustomerID, c.CompanyName;
SELECT COLUMN_NAME, EXTRA FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'northwind' AND TABLE_NAME = 'products' AND COLUMN_NAME = 'ProductID';

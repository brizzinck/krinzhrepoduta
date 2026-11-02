SELECT COLUMN_TYPE AS discount_type FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'northwind_raw' AND TABLE_NAME = 'order details' AND COLUMN_NAME = 'Discount';
SELECT COUNT(*) AS rows_total, SUM(Discount <> 0) AS rows_with_discount, MAX(Discount) AS max_discount
FROM northwind_raw.`order details`;
SELECT ROUND(SUM(od.UnitPrice * od.Quantity * (1 - od.Discount)), 4) AS revenue_1997
FROM northwind_raw.orders o JOIN northwind_raw.`order details` od ON od.OrderID = o.OrderID
WHERE YEAR(o.OrderDate) = 1997;
SELECT COLUMN_NAME, EXTRA FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'northwind_raw' AND TABLE_NAME = 'products' AND COLUMN_NAME = 'ProductID';

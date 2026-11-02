SELECT ProductID, ProductName, UnitPrice
FROM products
WHERE UnitPrice > 20
ORDER BY UnitPrice DESC, ProductName;

SELECT COUNT(*) AS rows_total FROM (
SELECT ProductID, ProductName, UnitPrice
FROM products
WHERE UnitPrice > 20
ORDER BY UnitPrice DESC, ProductName
) AS t;

-- 4. GROUP BY, HAVING, CASE (слайди 38-42)
SELECT Country, COUNT(*) AS customers FROM customers GROUP BY Country HAVING COUNT(*) >= 8 ORDER BY customers DESC, Country;
SELECT CategoryID, MIN(UnitPrice) AS min_p, MAX(UnitPrice) AS max_p, ROUND(AVG(UnitPrice), 2) AS avg_p FROM products GROUP BY CategoryID ORDER BY CategoryID;
SELECT ProductName, UnitPrice, CASE WHEN UnitPrice < 10 THEN 'cheap' WHEN UnitPrice < 50 THEN 'normal' ELSE 'expensive' END AS price_class
FROM products ORDER BY UnitPrice, ProductID LIMIT 6;
-- 5. JOIN: INNER, LEFT, емуляція FULL (слайди 44-54)
SELECT p.ProductName, c.CategoryName FROM products p INNER JOIN categories c ON c.CategoryID = p.CategoryID ORDER BY p.ProductID LIMIT 4;
SELECT c.CustomerID, c.CompanyName, o.OrderID FROM customers c LEFT JOIN orders o ON o.CustomerID = c.CustomerID WHERE o.OrderID IS NULL;
SELECT COUNT(*) AS inner_cnt FROM customers c INNER JOIN orders o ON o.CustomerID = c.CustomerID;
SELECT COUNT(*) AS left_cnt FROM customers c LEFT JOIN orders o ON o.CustomerID = c.CustomerID;
SELECT COUNT(*) AS full_emul_cnt FROM (SELECT c.CustomerID, o.OrderID FROM customers c LEFT JOIN orders o ON o.CustomerID = c.CustomerID UNION SELECT c.CustomerID, o.OrderID FROM customers c RIGHT JOIN orders o ON o.CustomerID = c.CustomerID) t;
SELECT COUNT(*) AS full_join_try FROM customers c FULL OUTER JOIN orders o ON o.CustomerID = c.CustomerID;

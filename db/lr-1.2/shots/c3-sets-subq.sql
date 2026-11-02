-- 6. Множинні операції (слайди 57-61)
SELECT Country FROM suppliers UNION SELECT Country FROM customers ORDER BY Country LIMIT 5;
SELECT COUNT(*) AS union_all_cnt FROM (SELECT Country FROM suppliers UNION ALL SELECT Country FROM customers) t;
SELECT COUNT(*) AS union_cnt FROM (SELECT Country FROM suppliers UNION SELECT Country FROM customers) t;
SELECT Country FROM suppliers INTERSECT SELECT Country FROM customers ORDER BY Country;
SELECT Country FROM suppliers EXCEPT SELECT Country FROM customers ORDER BY Country;
SELECT Country FROM suppliers MINUS SELECT Country FROM customers;
-- 7. Підзапити у WHERE, HAVING, FROM, SELECT (слайди 63-67)
SELECT ProductName, UnitPrice FROM products WHERE UnitPrice > (SELECT AVG(UnitPrice) FROM products) ORDER BY UnitPrice DESC LIMIT 5;
SELECT CompanyName FROM customers WHERE CustomerID NOT IN (SELECT CustomerID FROM orders);
SELECT c.CompanyName FROM customers c WHERE EXISTS (SELECT 1 FROM orders o WHERE o.CustomerID = c.CustomerID AND o.Freight > 800);
SELECT CategoryID, AVG(UnitPrice) AS avg_p FROM products GROUP BY CategoryID HAVING AVG(UnitPrice) > (SELECT AVG(UnitPrice) FROM products) ORDER BY CategoryID;
SELECT t.CategoryID, t.cnt FROM (SELECT CategoryID, COUNT(*) AS cnt FROM products GROUP BY CategoryID) t WHERE t.cnt > 10;

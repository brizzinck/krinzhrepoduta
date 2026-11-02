SELECT c.CategoryName, ROUND(AVG(p.UnitPrice), 2) AS avg_price
FROM products p
JOIN categories c ON c.CategoryID = p.CategoryID
GROUP BY c.CategoryID, c.CategoryName
ORDER BY c.CategoryName;

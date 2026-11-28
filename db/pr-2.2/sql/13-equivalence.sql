-- Крок 13. Переписані запити 5 і 6 дають той самий результат: однакова кількість рядків, EXCEPT в обидва боки дає 0.
-- Q5: GROUP BY c.CustomerID, c.CompanyName проти GROUP BY c.CustomerID. Q6: підзапит проти LEFT JOIN.
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


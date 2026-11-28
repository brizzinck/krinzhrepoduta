SELECT e.EmployeeID, e.FirstName, e.LastName, COUNT(*) AS orders
FROM employees e
JOIN orders o ON o.EmployeeID = e.EmployeeID
GROUP BY e.EmployeeID
ORDER BY orders DESC
LIMIT 5;

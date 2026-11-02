SELECT e.City,
       COUNT(DISTINCT e.EmployeeID) AS employees,
       COUNT(DISTINCT c.CustomerID) AS customers
FROM employees e
LEFT JOIN customers c ON c.City = e.City
GROUP BY e.City
ORDER BY e.City;

SELECT COUNT(*) AS rows_total FROM (
SELECT e.City,
       COUNT(DISTINCT e.EmployeeID) AS employees,
       COUNT(DISTINCT c.CustomerID) AS customers
FROM employees e
LEFT JOIN customers c ON c.City = e.City
GROUP BY e.City
ORDER BY e.City
) AS t;

-- без DISTINCT лічильники хибні: з'єднання множить рядки (4 співробітники x 6 клієнтів у Лондоні)
SELECT e.City, COUNT(e.EmployeeID) AS employees_wrong, COUNT(c.CustomerID) AS customers_wrong
FROM employees e LEFT JOIN customers c ON c.City = e.City
GROUP BY e.City ORDER BY e.City;

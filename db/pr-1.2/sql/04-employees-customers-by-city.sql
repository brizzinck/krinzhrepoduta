-- Запит 4. Кількість співробітників і клієнтів у містах зі співробітниками (LEFT JOIN, COUNT DISTINCT), 5 рядків.
SELECT e.City,
       COUNT(DISTINCT e.EmployeeID) AS employees,
       COUNT(DISTINCT c.CustomerID) AS customers
FROM employees e
LEFT JOIN customers c ON c.City = e.City
GROUP BY e.City
ORDER BY e.City;

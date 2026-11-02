SELECT o.OrderID, o.OrderDate, o.CustomerID,
       (SELECT CONCAT(e.FirstName, ' ', e.LastName)
        FROM employees e
        WHERE e.EmployeeID = o.EmployeeID) AS employee
FROM orders o
LIMIT 10;

SELECT COUNT(*) AS rows_total FROM (
SELECT o.OrderID, o.OrderDate, o.CustomerID,
       (SELECT CONCAT(e.FirstName, ' ', e.LastName)
        FROM employees e
        WHERE e.EmployeeID = o.EmployeeID) AS employee
FROM orders o
) AS t;

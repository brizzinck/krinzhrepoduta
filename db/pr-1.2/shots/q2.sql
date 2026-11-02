SELECT OrderID, CustomerID, OrderDate, DAYNAME(OrderDate) AS day_name
FROM orders
WHERE DAYOFWEEK(OrderDate) = 3
ORDER BY OrderDate
LIMIT 10;

SELECT COUNT(*) AS rows_total FROM (
SELECT OrderID, CustomerID, OrderDate, DAYNAME(OrderDate) AS day_name
FROM orders
WHERE DAYOFWEEK(OrderDate) = 3
ORDER BY OrderDate
) AS t;

SELECT SUM(DAYOFWEEK(OrderDate) = 3) AS dayofweek_eq_3, SUM(WEEKDAY(OrderDate) = 1) AS weekday_eq_1, SUM(DAYNAME(OrderDate) = 'Tuesday') AS dayname_tuesday FROM orders;

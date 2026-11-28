-- Запит 2. Замовлення, зроблені у вівторок (DAYOFWEEK: неділя = 1, вівторок = 3), 168 рядків.
SELECT OrderID, CustomerID, OrderDate, DAYNAME(OrderDate) AS day_name
FROM orders
WHERE DAYOFWEEK(OrderDate) = 3
ORDER BY OrderDate;

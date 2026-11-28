-- Запит 5. Дохід від кожного клієнта з урахуванням знижки (JOIN трьох таблиць), 89 рядків.
SELECT c.CustomerID, c.CompanyName,
       ROUND(SUM(od.UnitPrice * od.Quantity * (1 - od.Discount)), 2) AS revenue
FROM customers c
JOIN orders o ON o.CustomerID = c.CustomerID
JOIN `order details` od ON od.OrderID = o.OrderID
GROUP BY c.CustomerID, c.CompanyName
ORDER BY revenue DESC;

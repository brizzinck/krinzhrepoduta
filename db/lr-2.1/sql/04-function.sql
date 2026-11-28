-- Крок 4. Функція: на відміну від процедури, повертає значення і працює всередині SELECT
CREATE FUNCTION OrderTotal(p_order INT) RETURNS DECIMAL(12,2) READS SQL DATA
    RETURN (SELECT ROUND(SUM(UnitPrice * Quantity * (1 - Discount)), 2) FROM `order details` WHERE OrderID = p_order);
SELECT OrderID, OrderTotal(OrderID) AS total FROM orders ORDER BY OrderID LIMIT 3;

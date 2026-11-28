-- Запит 1. Продукти, ціна яких більша за 20 доларів (WHERE, ORDER BY), 37 рядків.
SELECT ProductID, ProductName, UnitPrice
FROM products
WHERE UnitPrice > 20
ORDER BY UnitPrice DESC, ProductName;

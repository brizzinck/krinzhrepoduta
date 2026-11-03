-- Перевірка: тригер створено; виклик процедури, потім вибірка з журналу
SELECT TRIGGER_NAME, ACTION_TIMING, EVENT_MANIPULATION, EVENT_OBJECT_TABLE
FROM information_schema.TRIGGERS WHERE TRIGGER_SCHEMA = 'northwind';
CALL AddProduct('Test Product B', 2, 2, '12 - 550 ml bottles', 10, 50, 0, 5, 0, @new_id);
SELECT * FROM productslog;
-- Один INSERT із двома рядками: тригер спрацьовує двічі (FOR EACH ROW)
INSERT INTO products (ProductName, SupplierID, CategoryID, UnitPrice, UnitsInStock)
VALUES ('Test Product C', 3, 3, 7, 10), ('Test Product D', 3, 3, 8, 20);
SELECT * FROM productslog;

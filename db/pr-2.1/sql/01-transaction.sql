-- Завдання 1. Оновлення адреси замовника в customers, ROLLBACK, рівень ізоляції READ UNCOMMITTED.
-- У MySQL SET TRANSACTION без слова SESSION діє лише на наступну транзакцію.
SELECT CustomerID, Address FROM customers WHERE CustomerID = 'ALFKI';

SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
START TRANSACTION;
UPDATE customers SET Address = 'вул. Тестова, 1' WHERE CustomerID = 'ALFKI';
SELECT CustomerID, Address FROM customers WHERE CustomerID = 'ALFKI';
ROLLBACK;

SELECT CustomerID, Address FROM customers WHERE CustomerID = 'ALFKI';

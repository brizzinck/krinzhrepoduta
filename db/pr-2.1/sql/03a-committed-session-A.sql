-- Сесія A: змінює адресу, утримує транзакцію 6 секунд, потім ROLLBACK.
SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
START TRANSACTION;
UPDATE customers SET Address = 'вул. Тестова, 1' WHERE CustomerID = 'ALFKI';
SELECT NOW(3) AS time_a, 'A: після UPDATE' AS step, Address FROM customers WHERE CustomerID = 'ALFKI';
DO SLEEP(6);
ROLLBACK;
SELECT NOW(3) AS time_a, 'A: після ROLLBACK' AS step, Address FROM customers WHERE CustomerID = 'ALFKI';

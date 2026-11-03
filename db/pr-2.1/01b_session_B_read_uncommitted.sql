-- Сесія B: читає ту саму адресу, поки транзакція A не завершена, і після її ROLLBACK. Рівень: READ UNCOMMITTED
DO SLEEP(2);
SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
START TRANSACTION;
SELECT NOW(3) AS time_b, 'B: поки A не завершила' AS step, Address FROM customers WHERE CustomerID = 'ALFKI';
DO SLEEP(6);
SELECT NOW(3) AS time_b, 'B: після ROLLBACK в A' AS step, Address FROM customers WHERE CustomerID = 'ALFKI';
COMMIT;

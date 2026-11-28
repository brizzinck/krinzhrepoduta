-- Крок 2. SAVEPOINT: відкат лише частини транзакції
START TRANSACTION;
UPDATE customers SET City = 'Kyiv' WHERE CustomerID = 'ALFKI';
SAVEPOINT after_city;
UPDATE customers SET Country = 'Ukraine' WHERE CustomerID = 'ALFKI';
ROLLBACK TO SAVEPOINT after_city;
COMMIT;
SELECT CustomerID, City, Country FROM customers WHERE CustomerID = 'ALFKI';

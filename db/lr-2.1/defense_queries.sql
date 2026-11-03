-- ЛР 2.1. Типові додаткові завдання на захисті ПР 2.1 (робота на копії бази northwind_defense).
USE northwind_defense;

-- 1. Транзакція зі списанням залишку й перевіркою (приклад викладачки, SERIALIZABLE), у MySQL IF лише в процедурі
DELIMITER $$
CREATE PROCEDURE ShipProduct(IN p_id INT, IN p_qty INT)
BEGIN
    DECLARE v_stock INT;
    SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;
    START TRANSACTION;
    SELECT UnitsInStock INTO v_stock FROM products WHERE ProductID = p_id FOR UPDATE;
    IF v_stock >= p_qty THEN
        UPDATE products SET UnitsInStock = UnitsInStock - p_qty WHERE ProductID = p_id;
        COMMIT;
        SELECT 'Success' AS result;
    ELSE
        ROLLBACK;
        SELECT 'Failed' AS result;
    END IF;
END$$
DELIMITER ;
SELECT UnitsInStock FROM products WHERE ProductID = 1;
CALL ShipProduct(1, 4);
CALL ShipProduct(1, 100000);
SELECT UnitsInStock FROM products WHERE ProductID = 1;

-- 2. SAVEPOINT: відкат лише частини транзакції
START TRANSACTION;
UPDATE customers SET City = 'Kyiv' WHERE CustomerID = 'ALFKI';
SAVEPOINT after_city;
UPDATE customers SET Country = 'Ukraine' WHERE CustomerID = 'ALFKI';
ROLLBACK TO SAVEPOINT after_city;
COMMIT;
SELECT CustomerID, City, Country FROM customers WHERE CustomerID = 'ALFKI';

-- 3. Тригер на зміну зі старими й новими значеннями (варіанти 6-10)
CREATE TABLE ProductsUpdateLog (
    LogID INT AUTO_INCREMENT PRIMARY KEY, ProductID INT,
    OldPrice DECIMAL(19,4), NewPrice DECIMAL(19,4), ChangedAt DATETIME DEFAULT CURRENT_TIMESTAMP);
CREATE TRIGGER trg_products_au AFTER UPDATE ON products FOR EACH ROW
    INSERT INTO ProductsUpdateLog (ProductID, OldPrice, NewPrice) VALUES (OLD.ProductID, OLD.UnitPrice, NEW.UnitPrice);
UPDATE products SET UnitPrice = UnitPrice + 1 WHERE ProductID = 2;
SELECT LogID, ProductID, OldPrice, NewPrice FROM ProductsUpdateLog;

-- 4. Функція: на відміну від процедури, повертає значення і працює всередині SELECT
CREATE FUNCTION OrderTotal(p_order INT) RETURNS DECIMAL(12,2) READS SQL DATA
    RETURN (SELECT ROUND(SUM(UnitPrice * Quantity * (1 - Discount)), 2) FROM `order details` WHERE OrderID = p_order);
SELECT OrderID, OrderTotal(OrderID) AS total FROM orders ORDER BY OrderID LIMIT 3;

-- 5. Просте представлення оновлюване, а з групуванням ні (помилка 1288)
CREATE VIEW v_german_customers AS SELECT CustomerID, CompanyName, City FROM customers WHERE Country = 'Germany';
UPDATE v_german_customers SET City = 'Berlin' WHERE CustomerID = 'ALFKI';
CREATE VIEW v_city_counts AS SELECT City, COUNT(*) AS cnt FROM customers GROUP BY City;
UPDATE v_city_counts SET cnt = 0 WHERE City = 'Berlin';

-- 6. Роль замість окремих привілеїв
CREATE ROLE 'nw_reader';
GRANT SELECT ON northwind_defense.* TO 'nw_reader';
CREATE USER 'lavrishko_ro'@'%' IDENTIFIED BY 'Lavr_dev_2026';
GRANT 'nw_reader' TO 'lavrishko_ro'@'%';
SHOW GRANTS FOR 'lavrishko_ro'@'%' USING 'nw_reader';
DROP USER 'lavrishko_ro'@'%';
DROP ROLE 'nw_reader';

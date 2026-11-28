-- Крок 5. Журнал productslog і тригер AFTER INSERT на products; тригер спрацьовує на кожен рядок.
-- Спершу викликається процедура з кроку 4, потім INSERT із двома рядками.
DROP TRIGGER IF EXISTS trg_products_ai;
DROP TABLE IF EXISTS productslog;
CREATE TABLE productslog (
    LogID        INT AUTO_INCREMENT PRIMARY KEY,
    ProductID    INT,
    ProductName  VARCHAR(40),
    SupplierID   INT,
    CategoryID   INT,
    UnitPrice    DECIMAL(19,4),
    UnitsInStock SMALLINT,
    LogDate      DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE TRIGGER trg_products_ai AFTER INSERT ON products FOR EACH ROW
    INSERT INTO productslog (ProductID, ProductName, SupplierID, CategoryID, UnitPrice, UnitsInStock)
    VALUES (NEW.ProductID, NEW.ProductName, NEW.SupplierID, NEW.CategoryID, NEW.UnitPrice, NEW.UnitsInStock);

-- Перевірка: тригер створено; виклик процедури, потім вибірка з журналу
SELECT TRIGGER_NAME, ACTION_TIMING, EVENT_MANIPULATION, EVENT_OBJECT_TABLE
FROM information_schema.TRIGGERS WHERE TRIGGER_SCHEMA = 'northwind';
CALL AddProduct('Test Product B', 2, 2, '12 - 550 ml bottles', 10, 50, 0, 5, 0, @new_id);
SELECT * FROM productslog;
-- Один INSERT із двома рядками: тригер спрацьовує двічі (FOR EACH ROW)
INSERT INTO products (ProductName, SupplierID, CategoryID, UnitPrice, UnitsInStock)
VALUES ('Test Product C', 3, 3, 7, 10), ('Test Product D', 3, 3, 8, 20);
SELECT * FROM productslog;

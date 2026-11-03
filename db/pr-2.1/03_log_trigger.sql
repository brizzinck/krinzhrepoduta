-- Завдання 3. Таблиця журналу productslog і тригер на додавання запису в products.
-- Імʼя таблиці без пробілу (у завданні «Products Log»). Замість віртуальної таблиці inserted (T-SQL) у MySQL використовується NEW:
-- тригер спрацьовує окремо для кожного вставленого рядка.
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

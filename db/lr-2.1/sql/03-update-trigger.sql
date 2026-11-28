-- Крок 3. Тригер на зміну зі старими й новими значеннями (варіанти 6-10)
CREATE TABLE ProductsUpdateLog (
    LogID INT AUTO_INCREMENT PRIMARY KEY, ProductID INT,
    OldPrice DECIMAL(19,4), NewPrice DECIMAL(19,4), ChangedAt DATETIME DEFAULT CURRENT_TIMESTAMP);
CREATE TRIGGER trg_products_au AFTER UPDATE ON products FOR EACH ROW
    INSERT INTO ProductsUpdateLog (ProductID, OldPrice, NewPrice) VALUES (OLD.ProductID, OLD.UnitPrice, NEW.UnitPrice);
UPDATE products SET UnitPrice = UnitPrice + 1 WHERE ProductID = 2;
SELECT LogID, ProductID, OldPrice, NewPrice FROM ProductsUpdateLog;

-- Завдання 2. Процедура додавання запису в таблицю products.
-- ProductID генерується сам (AUTO_INCREMENT виправлено в setup_northwind.sh), новий ідентифікатор повертається через OUT-параметр.
DROP PROCEDURE IF EXISTS AddProduct;
DELIMITER $$
CREATE PROCEDURE AddProduct(
    IN  p_name          VARCHAR(40),
    IN  p_supplier_id   INT,
    IN  p_category_id   INT,
    IN  p_qty_per_unit  VARCHAR(20),
    IN  p_unit_price    DECIMAL(19,4),
    IN  p_units_in_stock SMALLINT,
    IN  p_units_on_order SMALLINT,
    IN  p_reorder_level SMALLINT,
    IN  p_discontinued  TINYINT(1),
    OUT p_new_id        INT)
BEGIN
    INSERT INTO products (ProductName, SupplierID, CategoryID, QuantityPerUnit, UnitPrice,
                          UnitsInStock, UnitsOnOrder, ReorderLevel, Discontinued)
    VALUES (p_name, p_supplier_id, p_category_id, p_qty_per_unit, p_unit_price,
            p_units_in_stock, p_units_on_order, p_reorder_level, p_discontinued);
    SET p_new_id = LAST_INSERT_ID();
END$$
DELIMITER ;

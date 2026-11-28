-- Крок 1. Транзакція зі списанням залишку й перевіркою (приклад викладачки, SERIALIZABLE), у MySQL IF лише в процедурі
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

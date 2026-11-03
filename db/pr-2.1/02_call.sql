-- Виклик процедури (аналог EXEC у T-SQL)
CALL AddProduct('Test Product A', 1, 1, '10 boxes x 20 bags', 25.5, 100, 0, 10, 0, @new_id);
SELECT @new_id AS new_product_id;
SELECT ProductID, ProductName, UnitPrice, UnitsInStock FROM products ORDER BY ProductID DESC LIMIT 3;

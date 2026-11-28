-- Крок 3. Малоселективний стовпець: при Discontinued = 0 індекс майже нічого не відсіює.
-- Підготовка: індекс по Discontinued, свіжа статистика, розподіл значень (69 і 8 рядків із 77).
CREATE INDEX idx_products_disc ON products (Discontinued);
ANALYZE TABLE products;
SELECT Discontinued, COUNT(*) AS cnt FROM products GROUP BY Discontinued;

-- До: Discontinued = 0, 69 рядків із 77 (type ref, rows 69).
EXPLAIN SELECT ProductName FROM products WHERE Discontinued = 0;

-- Зміна: інше значення, Discontinued = 1.
-- Після: 8 рядків із 77, індекс виграє (type ref, rows 8).
EXPLAIN SELECT ProductName FROM products WHERE Discontinued = 1;

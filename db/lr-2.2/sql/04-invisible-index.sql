-- Крок 4. Невидимий індекс: показує план без індексу, не видаляючи його.
-- Потрібен індекс idx_orders_date з кроку 1.

-- Зміна: індекс стає невидимим для оптимізатора, дані індексу лишаються.
ALTER TABLE orders ALTER INDEX idx_orders_date INVISIBLE;

-- До: індекс INVISIBLE, плану з індексом немає (type ALL, rows 830).
EXPLAIN SELECT OrderID FROM orders WHERE OrderDate >= '1997-01-01' AND OrderDate < '1997-02-01';

-- Зміна: індекс знову видимий.
ALTER TABLE orders ALTER INDEX idx_orders_date VISIBLE;

-- Після: індекс VISIBLE (type range, rows 33).
EXPLAIN SELECT OrderID FROM orders WHERE OrderDate >= '1997-01-01' AND OrderDate < '1997-02-01';

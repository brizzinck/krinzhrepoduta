-- Крок 5. DISTINCT після JOIN замінюється на EXISTS (слайд 39 лекції 3).

-- До: DISTINCT після JOIN.
EXPLAIN SELECT DISTINCT c.CustomerID FROM customers c JOIN orders o ON o.CustomerID = c.CustomerID;

-- Зміна: JOIN із DISTINCT переписано на EXISTS.
-- Після: плани майже однакові, в Extra з'являється FirstMatch(c): MySQL перетворює EXISTS на напівз'єднання.
EXPLAIN SELECT c.CustomerID FROM customers c WHERE EXISTS (SELECT 1 FROM orders o WHERE o.CustomerID = c.CustomerID);

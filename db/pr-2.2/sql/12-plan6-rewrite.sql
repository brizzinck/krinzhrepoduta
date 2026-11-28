-- Крок 12. Запит 6: EXPLAIN ANALYZE, залежний підзапит і LEFT JOIN. Дивись loops у виводі.

-- До: скалярний підзапит у списку вибору, виконується для кожного замовлення (loops=830).
EXPLAIN ANALYZE SELECT o.OrderID, o.OrderDate, o.CustomerID,
  (SELECT CONCAT(e.FirstName,' ',e.LastName) FROM employees e WHERE e.EmployeeID=o.EmployeeID) AS employee
FROM orders o;

-- Зміна: підзапит замінено на LEFT JOIN із employees.
-- Після: Left hash join, обидві таблиці читаються по одному разу (loops=1).
EXPLAIN ANALYZE SELECT o.OrderID, o.OrderDate, o.CustomerID, CONCAT(e.FirstName,' ',e.LastName) AS employee
FROM orders o LEFT JOIN employees e ON e.EmployeeID=o.EmployeeID;

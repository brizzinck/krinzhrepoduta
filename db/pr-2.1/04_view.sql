-- Завдання 4. Представлення на основі запиту №6 з ПР 1.2 (замовлення та відповідні співробітники).
CREATE OR REPLACE VIEW v_orders_with_employees AS
SELECT o.OrderID, o.OrderDate, o.CustomerID,
       (SELECT CONCAT(e.FirstName, ' ', e.LastName)
        FROM employees e
        WHERE e.EmployeeID = o.EmployeeID) AS employee
FROM orders o;

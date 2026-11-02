-- 8. ON DELETE SET NULL: після видалення працівника його оренди лишаються, а посилання стає NULL
SELECT COUNT(*) AS rentals_of_employee_2 FROM rentals WHERE employee_id = 2;
SELECT COUNT(*) AS rentals_without_employee_before FROM rentals WHERE employee_id IS NULL;
DELETE FROM employees WHERE employee_id = 2;
SELECT COUNT(*) AS rentals_without_employee_after FROM rentals WHERE employee_id IS NULL;


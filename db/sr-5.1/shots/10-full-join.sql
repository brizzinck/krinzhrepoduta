SELECT e.employee_id, e.full_name, r.rental_id
FROM employees e FULL OUTER JOIN rentals r ON r.employee_id = e.employee_id;

WITH fj AS (
SELECT e.employee_id, e.full_name, r.rental_id
FROM employees e LEFT JOIN rentals r ON r.employee_id = e.employee_id
UNION
SELECT e.employee_id, e.full_name, r.rental_id
FROM employees e RIGHT JOIN rentals r ON r.employee_id = e.employee_id
)
SELECT COUNT(*) AS total_rows, SUM(employee_id IS NULL OR rental_id IS NULL) AS unpaired_rows FROM fj;

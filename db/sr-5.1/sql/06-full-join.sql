-- Крок 6. FULL OUTER JOIN дає ERROR 1064 (навмисно), далі емуляція LEFT JOIN UNION RIGHT JOIN: total_rows 32, unpaired_rows 7.
-- 10. Повне зовнішнє з'єднання (FULL OUTER JOIN)
-- у MySQL немає (помилка 1064)
SELECT e.employee_id, e.full_name, r.rental_id
FROM employees e FULL OUTER JOIN rentals r ON r.employee_id = e.employee_id;

-- емуляція: LEFT JOIN UNION RIGHT JOIN без фільтрів (32 рядки)
SELECT e.employee_id, e.full_name, r.rental_id
FROM employees e LEFT JOIN rentals r ON r.employee_id = e.employee_id
UNION
SELECT e.employee_id, e.full_name, r.rental_id
FROM employees e RIGHT JOIN rentals r ON r.employee_id = e.employee_id;

-- 32 рядки, з них 7 без пари (2 працівники без оренд і 5 оренд без працівника)
WITH fj AS (
SELECT e.employee_id, e.full_name, r.rental_id
FROM employees e LEFT JOIN rentals r ON r.employee_id = e.employee_id
UNION
SELECT e.employee_id, e.full_name, r.rental_id
FROM employees e RIGHT JOIN rentals r ON r.employee_id = e.employee_id
)
SELECT COUNT(*) AS total_rows, SUM(employee_id IS NULL OR rental_id IS NULL) AS unpaired_rows FROM fj;

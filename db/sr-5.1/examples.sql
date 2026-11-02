-- СР 5.1. Реляційна алгебра: приклади на базі car_rental (MySQL 8.4).
USE car_rental;

-- 1. Об'єднання (UNION)
SELECT customer_id FROM rentals WHERE pickup_branch_id = 3
UNION
SELECT customer_id FROM rentals WHERE pickup_branch_id = 4
ORDER BY customer_id;

SELECT COUNT(*) AS rows_union FROM (SELECT customer_id FROM rentals WHERE pickup_branch_id = 3 UNION SELECT customer_id FROM rentals WHERE pickup_branch_id = 4) t;
SELECT COUNT(*) AS rows_union_all FROM (SELECT customer_id FROM rentals WHERE pickup_branch_id = 3 UNION ALL SELECT customer_id FROM rentals WHERE pickup_branch_id = 4) t;

-- 2. Перетин (INTERSECT)
SELECT customer_id FROM rentals WHERE pickup_branch_id = 3
INTERSECT
SELECT customer_id FROM rentals WHERE pickup_branch_id = 4;

-- перетин через різницю: R1 - (R1 - R2)
(SELECT customer_id FROM rentals WHERE pickup_branch_id = 3)
EXCEPT
((SELECT customer_id FROM rentals WHERE pickup_branch_id = 3) EXCEPT (SELECT customer_id FROM rentals WHERE pickup_branch_id = 4));

-- 3. Різниця (EXCEPT); MINUS у MySQL відсутній
SELECT customer_id FROM rentals WHERE pickup_branch_id = 3
EXCEPT
SELECT customer_id FROM rentals WHERE pickup_branch_id = 4;

SELECT customer_id FROM rentals WHERE pickup_branch_id = 4
EXCEPT
SELECT customer_id FROM rentals WHERE pickup_branch_id = 3;

SELECT customer_id FROM rentals WHERE pickup_branch_id = 3
MINUS
SELECT customer_id FROM rentals WHERE pickup_branch_id = 4;

-- 4. Проєкція
SELECT brand, fuel_type FROM car_models;
SELECT DISTINCT brand, fuel_type FROM car_models;

-- 5. Вибірка (обмеження)
SELECT car_id, plate_number, daily_price, status
FROM cars
WHERE status = 'available' AND daily_price > 1500;

-- 6. Декартів добуток
SELECT b.city, e.name AS extra FROM branches b, extras e;

SELECT COUNT(*) AS rows_comma FROM branches b, extras e;
SELECT COUNT(*) AS rows_cross_join FROM branches b CROSS JOIN extras e;

-- 7. θ-з'єднання
SELECT r.rental_id, r.car_id, r.daily_price AS contract_price, c.daily_price AS current_price
FROM rentals r
JOIN cars c ON r.car_id = c.car_id AND r.daily_price < c.daily_price;

-- 8. Еквіз'єднання та природне з'єднання
SELECT * FROM car_classes k INNER JOIN car_models m ON m.class_id = k.class_id LIMIT 5;
SELECT * FROM car_classes NATURAL JOIN car_models LIMIT 5;
SELECT COUNT(*) AS rows_inner FROM car_classes k INNER JOIN car_models m ON m.class_id = k.class_id;
SELECT COUNT(*) AS rows_natural FROM car_classes NATURAL JOIN car_models;

-- 9. Напівз'єднання та антиз'єднання
SELECT c.customer_id, c.last_name
FROM customers c
WHERE EXISTS (SELECT 1 FROM rentals r WHERE r.customer_id = c.customer_id);

SELECT COUNT(*) AS rows_inner_join FROM customers c JOIN rentals r ON r.customer_id = c.customer_id;

SELECT c.customer_id, c.last_name
FROM customers c
LEFT JOIN rentals r ON r.customer_id = c.customer_id
WHERE r.rental_id IS NULL;

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

-- 11. Ділення
SELECT class_id, name FROM car_classes WHERE name IN ('Економ', 'Преміум');

SELECT c.customer_id, c.last_name
FROM customers c
WHERE NOT EXISTS (
  SELECT 1 FROM car_classes k
  WHERE k.name IN ('Економ', 'Преміум')
    AND NOT EXISTS (
      SELECT 1 FROM rentals r
      JOIN cars a ON a.car_id = r.car_id
      JOIN car_models m ON m.model_id = a.model_id
      WHERE r.customer_id = c.customer_id AND m.class_id = k.class_id AND r.status <> 'cancelled'));

SELECT c.customer_id, c.last_name
FROM customers c
WHERE NOT EXISTS (
  SELECT k.class_id FROM car_classes k WHERE k.name IN ('Економ', 'Преміум')
  EXCEPT
  SELECT m.class_id FROM rentals r
  JOIN cars a ON a.car_id = r.car_id
  JOIN car_models m ON m.model_id = a.model_id
  WHERE r.customer_id = c.customer_id AND r.status <> 'cancelled');

SELECT c.customer_id, c.last_name
FROM customers c
WHERE NOT EXISTS (
  SELECT 1 FROM car_classes k
  WHERE NOT EXISTS (
    SELECT 1 FROM rentals r
    JOIN cars a ON a.car_id = r.car_id
    JOIN car_models m ON m.model_id = a.model_id
    WHERE r.customer_id = c.customer_id AND m.class_id = k.class_id AND r.status <> 'cancelled'));

-- 12. Відмінності SQL: дублікати та NULL
SELECT COUNT(*) AS rows_without_distinct FROM (SELECT brand, fuel_type FROM car_models) t;
SELECT COUNT(*) AS rows_with_distinct FROM (SELECT DISTINCT brand, fuel_type FROM car_models) t;

SELECT COUNT(*) AS eq_null FROM rentals WHERE employee_id = NULL;
SELECT COUNT(*) AS is_null FROM rentals WHERE employee_id IS NULL;

SELECT COUNT(*) AS not_in_result FROM employees WHERE employee_id NOT IN (SELECT employee_id FROM rentals);
SELECT COUNT(*) AS not_exists_result FROM employees e WHERE NOT EXISTS (SELECT 1 FROM rentals r WHERE r.employee_id = e.employee_id);

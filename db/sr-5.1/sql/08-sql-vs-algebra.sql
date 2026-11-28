-- Крок 8. Відмінності SQL від алгебри: дублікати (12 проти 10) і NULL (eq_null 0 проти is_null 5, NOT IN проти NOT EXISTS).
-- 12. Відмінності SQL: дублікати та NULL
SELECT COUNT(*) AS rows_without_distinct FROM (SELECT brand, fuel_type FROM car_models) t;
SELECT COUNT(*) AS rows_with_distinct FROM (SELECT DISTINCT brand, fuel_type FROM car_models) t;

SELECT COUNT(*) AS eq_null FROM rentals WHERE employee_id = NULL;
SELECT COUNT(*) AS is_null FROM rentals WHERE employee_id IS NULL;

SELECT COUNT(*) AS not_in_result FROM employees WHERE employee_id NOT IN (SELECT employee_id FROM rentals);
SELECT COUNT(*) AS not_exists_result FROM employees e WHERE NOT EXISTS (SELECT 1 FROM rentals r WHERE r.employee_id = e.employee_id);

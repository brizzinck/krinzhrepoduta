-- Крок 4. Проєкція (з DISTINCT і без), вибірка (WHERE) і декартів добуток (кома і CROSS JOIN).
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

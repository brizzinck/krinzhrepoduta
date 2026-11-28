-- Крок 5. θ-, екві-, природне, напів- (EXISTS) та антиз'єднання (LEFT JOIN ... IS NULL).
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

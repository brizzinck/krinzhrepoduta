-- Крок 5. Вибірка із з'єднанням чотирьох таблиць: активні оренди.
SELECT r.rental_id, CONCAT(c.last_name, ' ', c.first_name) AS customer, CONCAT(m.brand, ' ', m.model_name) AS car, r.planned_end_at
FROM rentals r
JOIN customers c ON c.customer_id = r.customer_id
JOIN cars a ON a.car_id = r.car_id
JOIN car_models m ON m.model_id = a.model_id
WHERE r.status = 'active'
ORDER BY r.planned_end_at;

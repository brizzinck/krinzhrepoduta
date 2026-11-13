-- Запит для спостереження: активні оренди (JOIN п'яти таблиць), той самий, що в ПР 3.1
SELECT r.rental_id, CONCAT(c.last_name, ' ', c.first_name) AS customer,
       CONCAT(m.brand, ' ', m.model_name) AS car, b.city AS pickup_city
FROM rentals r
JOIN customers c ON c.customer_id = r.customer_id
JOIN cars a ON a.car_id = r.car_id
JOIN car_models m ON m.model_id = a.model_id
JOIN branches b ON b.branch_id = r.pickup_branch_id
WHERE r.status = 'active';

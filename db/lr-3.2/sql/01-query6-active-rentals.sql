-- Запит 6 (JOIN п'яти таблиць): меню пункт 6.
-- Звідки: src/main/java/rental/ReportDao.java, метод activeRentals(), анотація @SqlQuery.
-- Параметрів немає. Сценарій: demo/06-queries.txt.
SELECT r.rental_id, CONCAT(c.last_name, ' ', c.first_name) AS customer,
       CONCAT(m.brand, ' ', m.model_name) AS car, a.plate_number,
       b.city AS pickup_city, r.planned_end_at
FROM rentals r
JOIN customers c ON c.customer_id = r.customer_id
JOIN cars a ON a.car_id = r.car_id
JOIN car_models m ON m.model_id = a.model_id
JOIN branches b ON b.branch_id = r.pickup_branch_id
WHERE r.status = 'active'
ORDER BY r.planned_end_at;

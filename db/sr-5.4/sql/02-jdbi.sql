-- Версія 2 з 3: Jdbi, Micro-ORM (ПР 3.2). Звідки: pr-3.2/src/main/java/rental/ReportDao.java, анотації @SqlQuery.
-- Той самий SQL, що в 01-jdbc.sql, але параметри іменовані: :city і :maxPrice (@Bind("city"), @Bind("maxPrice")).
-- Jdbi замінює їх на ? і передає значення, на сервер іде той самий текст, що і в JDBC.
-- Значення в демо: :city = 'Київ', :maxPrice = 1500. База car_rental.

-- ReportDao.freeCars (порівнюваний запит)
SELECT a.car_id, CONCAT(m.brand, ' ', m.model_name) AS car, a.plate_number, b.city, a.daily_price
FROM cars a
JOIN car_models m ON m.model_id = a.model_id
JOIN branches b ON b.branch_id = a.branch_id
WHERE a.status = 'available' AND b.city = :city AND a.daily_price <= :maxPrice
ORDER BY a.daily_price;

-- ReportDao.activeRentals (параметрів немає)
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

-- ReportDao.branchRevenue (параметрів немає)
SELECT b.name AS branch, COUNT(DISTINCT r.rental_id) AS rentals,
       COUNT(p.payment_id) AS payments, SUM(p.amount) AS total,
       ROUND(AVG(p.amount), 2) AS avg_payment
FROM branches b
JOIN rentals r ON r.pickup_branch_id = b.branch_id
JOIN payments p ON p.rental_id = r.rental_id
WHERE p.kind IN ('rent', 'fine')
GROUP BY b.branch_id, b.name
ORDER BY total DESC;

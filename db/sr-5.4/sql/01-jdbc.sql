-- Версія 1 з 3: JDBC (ПР 3.1). Звідки: pr-3.1/src/main/java/rental/QueryDao.java, константи FREE_CARS,
-- ACTIVE_RENTALS, BRANCH_REVENUE; текст у Java-рядку передається в connection.prepareStatement(...) без змін.
-- Порівнюваний у звіті запит: freeCars (два параметри). Параметри за номером: ps.setString(1, city), ps.setBigDecimal(2, maxPrice).
-- Значення в демо: 1 = 'Київ', 2 = 1500. База car_rental.

-- QueryDao.freeCars (порівнюваний запит з розділу "Прямий доступ через API")
SELECT a.car_id, CONCAT(m.brand, ' ', m.model_name) AS car, a.plate_number, b.city, a.daily_price
FROM cars a
JOIN car_models m ON m.model_id = a.model_id
JOIN branches b ON b.branch_id = a.branch_id
WHERE a.status = 'available' AND b.city = ? AND a.daily_price <= ?
ORDER BY a.daily_price;

-- QueryDao.activeRentals (параметрів немає)
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

-- QueryDao.branchRevenue (параметрів немає)
SELECT b.name AS branch, COUNT(DISTINCT r.rental_id) AS rentals, COUNT(p.payment_id) AS payments,
       SUM(p.amount) AS total, ROUND(AVG(p.amount), 2) AS avg_payment
FROM branches b
JOIN rentals r ON r.pickup_branch_id = b.branch_id
JOIN payments p ON p.rental_id = r.rental_id
WHERE p.kind IN ('rent', 'fine')
GROUP BY b.branch_id, b.name
ORDER BY total DESC;

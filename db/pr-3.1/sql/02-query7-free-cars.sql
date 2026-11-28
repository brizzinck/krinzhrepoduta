-- Запит 7 (фільтр з параметрами): меню пункт 7.
-- Звідки: src/main/java/rental/QueryDao.java, метод freeCars(String city, BigDecimal maxPrice), константа FREE_CARS.
-- Значення з demo/06-queries.txt: перший ? = 'Київ' (setString), другий ? = 1500 (setBigDecimal).
SELECT a.car_id, CONCAT(m.brand, ' ', m.model_name) AS car, a.plate_number, b.city, a.daily_price
FROM cars a
JOIN car_models m ON m.model_id = a.model_id
JOIN branches b ON b.branch_id = a.branch_id
WHERE a.status = 'available' AND b.city = ? AND a.daily_price <= ?
ORDER BY a.daily_price;

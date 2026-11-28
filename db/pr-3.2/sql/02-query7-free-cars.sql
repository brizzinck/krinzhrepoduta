-- Запит 7 (фільтр з іменованими параметрами): меню пункт 7.
-- Звідки: src/main/java/rental/ReportDao.java, метод freeCars(String city, BigDecimal maxPrice), анотація @SqlQuery.
-- Jdbi замінює :city і :maxPrice на ? і передає значення з @Bind.
-- Значення з demo/06-queries.txt: :city = 'Київ', :maxPrice = 1500.
SELECT a.car_id, CONCAT(m.brand, ' ', m.model_name) AS car, a.plate_number, b.city, a.daily_price
FROM cars a
JOIN car_models m ON m.model_id = a.model_id
JOIN branches b ON b.branch_id = a.branch_id
WHERE a.status = 'available' AND b.city = :city AND a.daily_price <= :maxPrice
ORDER BY a.daily_price;

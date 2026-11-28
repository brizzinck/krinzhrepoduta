-- Запит 8 (агрегація): меню пункт 8.
-- Звідки: src/main/java/rental/ReportDao.java, метод branchRevenue(), анотація @SqlQuery.
-- Параметрів немає. Сценарій: demo/06-queries.txt.
SELECT b.name AS branch, COUNT(DISTINCT r.rental_id) AS rentals,
       COUNT(p.payment_id) AS payments, SUM(p.amount) AS total,
       ROUND(AVG(p.amount), 2) AS avg_payment
FROM branches b
JOIN rentals r ON r.pickup_branch_id = b.branch_id
JOIN payments p ON p.rental_id = r.rental_id
WHERE p.kind IN ('rent', 'fine')
GROUP BY b.branch_id, b.name
ORDER BY total DESC;

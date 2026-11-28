-- Крок 1. Об'єднання UNION і UNION ALL: UNION прибирає дублікати (rows_union 13), UNION ALL ні (rows_union_all 20).
-- 1. Об'єднання (UNION)
SELECT customer_id FROM rentals WHERE pickup_branch_id = 3
UNION
SELECT customer_id FROM rentals WHERE pickup_branch_id = 4
ORDER BY customer_id;

SELECT COUNT(*) AS rows_union FROM (SELECT customer_id FROM rentals WHERE pickup_branch_id = 3 UNION SELECT customer_id FROM rentals WHERE pickup_branch_id = 4) t;
SELECT COUNT(*) AS rows_union_all FROM (SELECT customer_id FROM rentals WHERE pickup_branch_id = 3 UNION ALL SELECT customer_id FROM rentals WHERE pickup_branch_id = 4) t;

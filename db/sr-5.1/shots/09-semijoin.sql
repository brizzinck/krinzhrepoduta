SELECT c.customer_id, c.last_name
FROM customers c
WHERE EXISTS (SELECT 1 FROM rentals r WHERE r.customer_id = c.customer_id);

SELECT COUNT(*) AS rows_inner_join FROM customers c JOIN rentals r ON r.customer_id = c.customer_id;

SELECT c.customer_id, c.last_name
FROM customers c
LEFT JOIN rentals r ON r.customer_id = c.customer_id
WHERE r.rental_id IS NULL;

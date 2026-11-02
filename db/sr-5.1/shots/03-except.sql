SELECT customer_id FROM rentals WHERE pickup_branch_id = 3
EXCEPT
SELECT customer_id FROM rentals WHERE pickup_branch_id = 4;

SELECT customer_id FROM rentals WHERE pickup_branch_id = 4
EXCEPT
SELECT customer_id FROM rentals WHERE pickup_branch_id = 3;

SELECT customer_id FROM rentals WHERE pickup_branch_id = 3
MINUS
SELECT customer_id FROM rentals WHERE pickup_branch_id = 4;

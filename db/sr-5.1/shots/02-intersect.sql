SELECT customer_id FROM rentals WHERE pickup_branch_id = 3
INTERSECT
SELECT customer_id FROM rentals WHERE pickup_branch_id = 4;

-- перетин через різницю: R1 - (R1 - R2)
(SELECT customer_id FROM rentals WHERE pickup_branch_id = 3)
EXCEPT
((SELECT customer_id FROM rentals WHERE pickup_branch_id = 3) EXCEPT (SELECT customer_id FROM rentals WHERE pickup_branch_id = 4));

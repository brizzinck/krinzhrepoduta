-- Крок 3. Різниця EXCEPT в обидва боки (не комутативна); MINUS у MySQL немає, тому ERROR 1064 навмисна.
-- 3. Різниця (EXCEPT); MINUS у MySQL відсутній
SELECT customer_id FROM rentals WHERE pickup_branch_id = 3
EXCEPT
SELECT customer_id FROM rentals WHERE pickup_branch_id = 4;

SELECT customer_id FROM rentals WHERE pickup_branch_id = 4
EXCEPT
SELECT customer_id FROM rentals WHERE pickup_branch_id = 3;

SELECT customer_id FROM rentals WHERE pickup_branch_id = 3
MINUS
SELECT customer_id FROM rentals WHERE pickup_branch_id = 4;

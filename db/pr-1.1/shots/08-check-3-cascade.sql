-- 7. ON DELETE CASCADE: разом з орендою видаляється її склад (послуги)
INSERT INTO rentals (customer_id, car_id, employee_id, pickup_branch_id, start_at, planned_end_at, daily_price, status)
VALUES (19, 2, 4, 2, '2026-12-01 10:00:00', '2026-12-05 10:00:00', 850.00, 'booked');
SET @rid = LAST_INSERT_ID();
INSERT INTO rental_extras (rental_id, extra_id, quantity) VALUES (@rid, 1, 1), (@rid, 2, 1);
SELECT COUNT(*) AS extras_before_delete FROM rental_extras WHERE rental_id = @rid;
DELETE FROM rentals WHERE rental_id = @rid;
SELECT COUNT(*) AS extras_after_delete FROM rental_extras WHERE rental_id = @rid;


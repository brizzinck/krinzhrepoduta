-- Крок 4. Видалення авто 2 без оренд: поліс видаляється каскадом (policies_left 1).
DELETE FROM cars WHERE car_id = 2;
SELECT COUNT(*) AS policies_left FROM insurance_policies;

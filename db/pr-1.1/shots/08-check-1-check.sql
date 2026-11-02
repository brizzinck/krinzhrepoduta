-- 1. CHECK: добова ціна автомобіля має бути більшою за 0
INSERT INTO cars (model_id, branch_id, vin, plate_number, manufacture_year, mileage_km, daily_price, status)
VALUES (1, 1, 'VF1TESTCHECK00001', 'AA0001AA', 2022, 0, 0.00, 'available');

-- 2. CHECK: недопустиме значення статусу
INSERT INTO cars (model_id, branch_id, vin, plate_number, manufacture_year, mileage_km, daily_price, status)
VALUES (1, 1, 'VF1TESTCHECK00002', 'AA0002AA', 2022, 0, 900.00, 'sold');

-- 3. CHECK: планова дата повернення раніше за дату видачі
INSERT INTO rentals (customer_id, car_id, employee_id, pickup_branch_id, start_at, planned_end_at, daily_price, status)
VALUES (1, 2, 4, 2, '2026-12-10 10:00:00', '2026-12-09 10:00:00', 850.00, 'booked');


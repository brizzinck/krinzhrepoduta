-- 4. UNIQUE: повторний VIN
INSERT INTO cars (model_id, branch_id, vin, plate_number, manufacture_year, mileage_km, daily_price, status)
SELECT 1, 1, vin, 'AA0003AA', 2022, 0, 900.00, 'available' FROM cars WHERE car_id = 2;

-- 5. FOREIGN KEY: неіснуюча модель
INSERT INTO cars (model_id, branch_id, vin, plate_number, manufacture_year, mileage_km, daily_price, status)
VALUES (999, 1, 'VF1TESTCHECK00004', 'AA0004AA', 2022, 0, 900.00, 'available');

-- 6. ON DELETE RESTRICT: філіал, до якого прив'язані автомобілі, видалити не можна
DELETE FROM branches WHERE branch_id = 1;


-- ПР 1.1. Перевірка обмежень цілісності. Виконується на копії бази (car_rental_check), щоб невдалі вставки
-- не змінювали лічильники AUTO_INCREMENT основної бази. Очікуються повідомлення про помилки.
-- Запускати з --force: ../_shared/mysql.sh --force -t -vvv < check.sql (без нього клієнт зупиниться на першій помилці).
USE car_rental_check;

-- 1. CHECK: добова ціна автомобіля має бути більшою за 0
INSERT INTO cars (model_id, branch_id, vin, plate_number, manufacture_year, mileage_km, daily_price, status)
VALUES (1, 1, 'VF1TESTCHECK00001', 'AA0001AA', 2022, 0, 0.00, 'available');

-- 2. CHECK: недопустиме значення статусу
INSERT INTO cars (model_id, branch_id, vin, plate_number, manufacture_year, mileage_km, daily_price, status)
VALUES (1, 1, 'VF1TESTCHECK00002', 'AA0002AA', 2022, 0, 900.00, 'sold');

-- 3. CHECK: планова дата повернення раніше за дату видачі
INSERT INTO rentals (customer_id, car_id, employee_id, pickup_branch_id, start_at, planned_end_at, daily_price, status)
VALUES (1, 2, 4, 2, '2026-12-10 10:00:00', '2026-12-09 10:00:00', 850.00, 'booked');

-- 4. UNIQUE: повторний VIN
INSERT INTO cars (model_id, branch_id, vin, plate_number, manufacture_year, mileage_km, daily_price, status)
SELECT 1, 1, vin, 'AA0003AA', 2022, 0, 900.00, 'available' FROM cars WHERE car_id = 2;

-- 5. FOREIGN KEY: неіснуюча модель
INSERT INTO cars (model_id, branch_id, vin, plate_number, manufacture_year, mileage_km, daily_price, status)
VALUES (999, 1, 'VF1TESTCHECK00004', 'AA0004AA', 2022, 0, 900.00, 'available');

-- 6. ON DELETE RESTRICT: філіал, до якого прив'язані автомобілі, видалити не можна
DELETE FROM branches WHERE branch_id = 1;

-- 7. ON DELETE CASCADE: разом з орендою видаляється її склад (послуги)
INSERT INTO rentals (customer_id, car_id, employee_id, pickup_branch_id, start_at, planned_end_at, daily_price, status)
VALUES (19, 2, 4, 2, '2026-12-01 10:00:00', '2026-12-05 10:00:00', 850.00, 'booked');
SET @rid = LAST_INSERT_ID();
INSERT INTO rental_extras (rental_id, extra_id, quantity) VALUES (@rid, 1, 1), (@rid, 2, 1);
SELECT COUNT(*) AS extras_before_delete FROM rental_extras WHERE rental_id = @rid;
DELETE FROM rentals WHERE rental_id = @rid;
SELECT COUNT(*) AS extras_after_delete FROM rental_extras WHERE rental_id = @rid;

-- 8. ON DELETE SET NULL: після видалення працівника його оренди лишаються, а посилання стає NULL
SELECT COUNT(*) AS rentals_of_employee_2 FROM rentals WHERE employee_id = 2;
SELECT COUNT(*) AS rentals_without_employee_before FROM rentals WHERE employee_id IS NULL;
DELETE FROM employees WHERE employee_id = 2;
SELECT COUNT(*) AS rentals_without_employee_after FROM rentals WHERE employee_id IS NULL;

-- 9. ON DELETE SET DEFAULT: MySQL приймає опис, але InnoDB дію не виконує (видалення батьківського рядка відхиляється)
INSERT INTO branches (branch_id, name, city, address, phone) VALUES (5, 'Тестовий пункт', 'Харків', 'вул. Тестова, 1', '+380575550105');
CREATE TABLE demo_set_default (
    id INT PRIMARY KEY,
    branch_id INT NOT NULL DEFAULT 1,
    FOREIGN KEY (branch_id) REFERENCES branches (branch_id) ON DELETE SET DEFAULT
);
INSERT INTO demo_set_default (id, branch_id) VALUES (1, 5);
DELETE FROM branches WHERE branch_id = 5;
SELECT * FROM demo_set_default;

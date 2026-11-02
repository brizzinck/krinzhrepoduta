-- ЛР 1.1. Типові додаткові завдання на захисті ПР 1.1 (робота на копії бази car_rental_defense).
USE car_rental_defense;

-- 1. Створити нову таблицю зі зв'язком до наявної
CREATE TABLE insurance_policies (
    policy_id     INT           NOT NULL AUTO_INCREMENT,
    car_id        INT           NOT NULL,
    insurer       VARCHAR(60)   NOT NULL,
    policy_number VARCHAR(30)   NOT NULL,
    valid_from    DATE          NOT NULL,
    valid_to      DATE          NOT NULL,
    premium       DECIMAL(8, 2) NOT NULL,
    CONSTRAINT pk_insurance_policies PRIMARY KEY (policy_id),
    CONSTRAINT fk_insurance_car FOREIGN KEY (car_id) REFERENCES cars (car_id) ON DELETE CASCADE,
    CONSTRAINT uq_insurance_number UNIQUE (policy_number),
    CONSTRAINT chk_insurance_dates CHECK (valid_to > valid_from),
    CONSTRAINT chk_insurance_premium CHECK (premium > 0)
);
INSERT INTO insurance_policies (car_id, insurer, policy_number, valid_from, valid_to, premium) VALUES
(2, 'Страхова компанія «Оберіг»', 'EP-100234', '2026-01-01', '2026-12-31', 8200.00),
(5, 'Страхова компанія «Оберіг»', 'EP-100235', '2026-03-01', '2027-02-28', 9100.00);
SELECT * FROM insurance_policies;

-- 2. Змінити структуру таблиці й внести зміни в наявні дані
ALTER TABLE cars ADD COLUMN color VARCHAR(20) NULL;
UPDATE cars SET color = 'білий' WHERE car_id IN (2, 3, 4);
UPDATE cars SET color = 'чорний' WHERE car_id = 5;
SELECT car_id, plate_number, color FROM cars WHERE color IS NOT NULL;

-- 3. Масово змінити дані (підвищити ставку класу «Економ» на 10 %)
SELECT name, daily_base_rate FROM car_classes WHERE name = 'Економ';
UPDATE car_classes SET daily_base_rate = ROUND(daily_base_rate * 1.10, 2) WHERE name = 'Економ';
SELECT name, daily_base_rate FROM car_classes WHERE name = 'Економ';

-- 4. Видалити тестовий рядок і побачити каскад
DELETE FROM cars WHERE car_id = 2;
SELECT COUNT(*) AS policies_left FROM insurance_policies;

-- 5. Вибірка із з'єднанням: хто зараз орендує автомобілі
SELECT r.rental_id, CONCAT(c.last_name, ' ', c.first_name) AS customer, CONCAT(m.brand, ' ', m.model_name) AS car, r.planned_end_at
FROM rentals r
JOIN customers c ON c.customer_id = r.customer_id
JOIN cars a ON a.car_id = r.car_id
JOIN car_models m ON m.model_id = a.model_id
WHERE r.status = 'active'
ORDER BY r.planned_end_at;

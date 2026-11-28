-- Крок 1. Створення нової таблиці insurance_policies зі зв'язком до cars і двох полісів.
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

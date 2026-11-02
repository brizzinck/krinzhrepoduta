-- ПР 1.1. Предметна область «22. Прокат автомобілів».
-- Фізична модель для MySQL 8.4 (InnoDB, utf8mb4). Скрипт можна запускати повторно: базу буде створено заново.

DROP DATABASE IF EXISTS car_rental;
CREATE DATABASE car_rental
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;
USE car_rental;

-- 1. Пункти прокату (філіали)
CREATE TABLE branches (
    branch_id  INT          NOT NULL AUTO_INCREMENT,
    name       VARCHAR(60)  NOT NULL,
    city       VARCHAR(40)  NOT NULL,
    address    VARCHAR(100) NOT NULL,
    phone      VARCHAR(20)  NOT NULL,
    CONSTRAINT pk_branches PRIMARY KEY (branch_id),
    CONSTRAINT uq_branches_name UNIQUE (name),
    CONSTRAINT uq_branches_city_address UNIQUE (city, address)
);

-- 2. Класи автомобілів (економ, комфорт, позашляховик тощо)
CREATE TABLE car_classes (
    class_id         INT           NOT NULL AUTO_INCREMENT,
    name             VARCHAR(30)   NOT NULL,
    daily_base_rate  DECIMAL(8, 2) NOT NULL,
    deposit_amount   DECIMAL(8, 2) NOT NULL,
    CONSTRAINT pk_car_classes PRIMARY KEY (class_id),
    CONSTRAINT uq_car_classes_name UNIQUE (name),
    CONSTRAINT chk_car_classes_rate CHECK (daily_base_rate > 0),
    CONSTRAINT chk_car_classes_deposit CHECK (deposit_amount >= 0)
);

-- 3. Марки й моделі автомобілів
CREATE TABLE car_models (
    model_id      INT         NOT NULL AUTO_INCREMENT,
    class_id      INT         NOT NULL,
    brand         VARCHAR(30) NOT NULL,
    model_name    VARCHAR(40) NOT NULL,
    seats         TINYINT     NOT NULL,
    transmission  VARCHAR(10) NOT NULL,
    fuel_type     VARCHAR(10) NOT NULL,
    CONSTRAINT pk_car_models PRIMARY KEY (model_id),
    CONSTRAINT fk_car_models_class FOREIGN KEY (class_id)
        REFERENCES car_classes (class_id) ON DELETE RESTRICT,
    CONSTRAINT uq_car_models_brand_model UNIQUE (brand, model_name),
    CONSTRAINT chk_car_models_seats CHECK (seats BETWEEN 2 AND 9),
    CONSTRAINT chk_car_models_transmission CHECK (transmission IN ('manual', 'automatic')),
    CONSTRAINT chk_car_models_fuel CHECK (fuel_type IN ('petrol', 'diesel', 'hybrid', 'electric'))
);

-- 4. Автомобілі парку
CREATE TABLE cars (
    car_id            INT           NOT NULL AUTO_INCREMENT,
    model_id          INT           NOT NULL,
    branch_id         INT           NOT NULL,
    vin               CHAR(17)      NOT NULL,
    plate_number      VARCHAR(10)   NOT NULL,
    manufacture_year  SMALLINT      NOT NULL,
    mileage_km        INT UNSIGNED  NOT NULL DEFAULT 0,
    daily_price       DECIMAL(8, 2) NOT NULL,
    status            VARCHAR(12)   NOT NULL DEFAULT 'available',
    CONSTRAINT pk_cars PRIMARY KEY (car_id),
    CONSTRAINT fk_cars_model FOREIGN KEY (model_id)
        REFERENCES car_models (model_id) ON DELETE RESTRICT,
    CONSTRAINT fk_cars_branch FOREIGN KEY (branch_id)
        REFERENCES branches (branch_id) ON DELETE RESTRICT,
    CONSTRAINT uq_cars_vin UNIQUE (vin),
    CONSTRAINT uq_cars_plate UNIQUE (plate_number),
    CONSTRAINT chk_cars_year CHECK (manufacture_year BETWEEN 2005 AND 2030),
    CONSTRAINT chk_cars_price CHECK (daily_price > 0),
    CONSTRAINT chk_cars_status CHECK (status IN ('available', 'rented', 'maintenance', 'retired'))
);

-- 5. Клієнти
CREATE TABLE customers (
    customer_id      INT          NOT NULL AUTO_INCREMENT,
    first_name       VARCHAR(40)  NOT NULL,
    last_name        VARCHAR(40)  NOT NULL,
    middle_name      VARCHAR(40)  NULL,
    phone            VARCHAR(20)  NOT NULL,
    email            VARCHAR(80)  NOT NULL,
    birth_date       DATE         NOT NULL,
    license_number   VARCHAR(15)  NOT NULL,
    license_expires  DATE         NOT NULL,
    registered_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_customers PRIMARY KEY (customer_id),
    CONSTRAINT uq_customers_phone UNIQUE (phone),
    CONSTRAINT uq_customers_email UNIQUE (email),
    CONSTRAINT uq_customers_license UNIQUE (license_number),
    CONSTRAINT chk_customers_license_dates CHECK (license_expires > birth_date)
);

-- 6. Працівники пунктів прокату
CREATE TABLE employees (
    employee_id  INT          NOT NULL AUTO_INCREMENT,
    branch_id    INT          NOT NULL,
    full_name    VARCHAR(100) NOT NULL,
    position     VARCHAR(10)  NOT NULL,
    phone        VARCHAR(20)  NOT NULL,
    hired_at     DATE         NOT NULL,
    CONSTRAINT pk_employees PRIMARY KEY (employee_id),
    CONSTRAINT fk_employees_branch FOREIGN KEY (branch_id)
        REFERENCES branches (branch_id) ON DELETE RESTRICT,
    CONSTRAINT uq_employees_phone UNIQUE (phone),
    CONSTRAINT chk_employees_position CHECK (position IN ('manager', 'agent', 'mechanic'))
);

-- 7. Договори оренди
-- daily_price: ціна доби на момент укладання договору (свідома денормалізація, бо тариф авто з часом змінюється)
CREATE TABLE rentals (
    rental_id          INT           NOT NULL AUTO_INCREMENT,
    customer_id        INT           NOT NULL,
    car_id             INT           NOT NULL,
    employee_id        INT           NULL,
    pickup_branch_id   INT           NOT NULL,
    return_branch_id   INT           NULL,
    start_at           DATETIME      NOT NULL,
    planned_end_at     DATETIME      NOT NULL,
    actual_end_at      DATETIME      NULL,
    daily_price        DECIMAL(8, 2) NOT NULL,
    status             VARCHAR(10)   NOT NULL DEFAULT 'booked',
    CONSTRAINT pk_rentals PRIMARY KEY (rental_id),
    CONSTRAINT fk_rentals_customer FOREIGN KEY (customer_id)
        REFERENCES customers (customer_id) ON DELETE RESTRICT,
    CONSTRAINT fk_rentals_car FOREIGN KEY (car_id)
        REFERENCES cars (car_id) ON DELETE RESTRICT,
    CONSTRAINT fk_rentals_employee FOREIGN KEY (employee_id)
        REFERENCES employees (employee_id) ON DELETE SET NULL,
    CONSTRAINT fk_rentals_pickup FOREIGN KEY (pickup_branch_id)
        REFERENCES branches (branch_id) ON DELETE RESTRICT,
    CONSTRAINT fk_rentals_return FOREIGN KEY (return_branch_id)
        REFERENCES branches (branch_id) ON DELETE RESTRICT,
    CONSTRAINT chk_rentals_planned CHECK (planned_end_at > start_at),
    CONSTRAINT chk_rentals_actual CHECK (actual_end_at IS NULL OR actual_end_at >= start_at),
    CONSTRAINT chk_rentals_price CHECK (daily_price > 0),
    CONSTRAINT chk_rentals_status CHECK (status IN ('booked', 'active', 'closed', 'cancelled'))
);

-- 8. Платежі за оренду
CREATE TABLE payments (
    payment_id  INT           NOT NULL AUTO_INCREMENT,
    rental_id   INT           NOT NULL,
    amount      DECIMAL(10, 2) NOT NULL,
    kind        VARCHAR(10)   NOT NULL,
    method      VARCHAR(10)   NOT NULL,
    paid_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_payments PRIMARY KEY (payment_id),
    CONSTRAINT fk_payments_rental FOREIGN KEY (rental_id)
        REFERENCES rentals (rental_id) ON DELETE RESTRICT,
    CONSTRAINT chk_payments_amount CHECK (amount > 0),
    CONSTRAINT chk_payments_kind CHECK (kind IN ('deposit', 'rent', 'fine', 'refund')),
    CONSTRAINT chk_payments_method CHECK (method IN ('cash', 'card', 'online'))
);

-- 9. Додаткові послуги
CREATE TABLE extras (
    extra_id       INT           NOT NULL AUTO_INCREMENT,
    name           VARCHAR(50)   NOT NULL,
    price_per_day  DECIMAL(6, 2) NOT NULL,
    CONSTRAINT pk_extras PRIMARY KEY (extra_id),
    CONSTRAINT uq_extras_name UNIQUE (name),
    CONSTRAINT chk_extras_price CHECK (price_per_day >= 0)
);

-- 10. Склад оренди (зв'язок «багато до багатьох» між орендами й послугами)
CREATE TABLE rental_extras (
    rental_id  INT     NOT NULL,
    extra_id   INT     NOT NULL,
    quantity   TINYINT NOT NULL DEFAULT 1,
    CONSTRAINT pk_rental_extras PRIMARY KEY (rental_id, extra_id),
    CONSTRAINT fk_rental_extras_rental FOREIGN KEY (rental_id)
        REFERENCES rentals (rental_id) ON DELETE CASCADE,
    CONSTRAINT fk_rental_extras_extra FOREIGN KEY (extra_id)
        REFERENCES extras (extra_id) ON DELETE RESTRICT,
    CONSTRAINT chk_rental_extras_qty CHECK (quantity > 0)
);

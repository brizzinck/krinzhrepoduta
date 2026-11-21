-- Micromobility rental (e-scooters, e-bikes): physical model for MySQL 8.4 (InnoDB, utf8mb4).
-- Flyway migration V1. The database itself is created by Docker (MYSQL_DATABASE=micromobility).
-- Money is integer kopecks. Times are DATETIME in UTC. Every constraint and index has a name.
-- Every primary key is `id`; a foreign key column is `<entity>_id`. Every foreign key is RESTRICT.
-- Fixed lists are TINYINT UNSIGNED codes with a range CHECK; the app maps them to Java enums:
--   vehicle_type: 1 E_SCOOTER, 2 E_BIKE
--   vehicles.status: 1 AVAILABLE, 2 IN_RIDE, 3 CHARGING, 4 REPAIR, 5 RETIRED
--   payments.kind: 1 BONUS, 2 TOPUP, 3 RIDE, 4 REFUND

-- 1. Operators: companies whose vehicles are rented through the system
CREATE TABLE operators (
    id              INT          NOT NULL AUTO_INCREMENT,
    name            VARCHAR(40)  NOT NULL,
    phone           VARCHAR(13)  NOT NULL,
    email           VARCHAR(100) NOT NULL,
    commission_pct  DECIMAL(4,2) NOT NULL DEFAULT 10.00,
    CONSTRAINT pk_operators PRIMARY KEY (id),
    CONSTRAINT uq_operators_name UNIQUE (name),
    CONSTRAINT chk_operators_name CHECK (CHAR_LENGTH(TRIM(name)) >= 2),
    CONSTRAINT chk_operators_phone CHECK (phone REGEXP '^\\+380[0-9]{9}$'),
    CONSTRAINT chk_operators_email CHECK (email LIKE '_%@_%._%'),
    CONSTRAINT chk_operators_commission CHECK (commission_pct BETWEEN 0 AND 50)
) ENGINE = InnoDB;

-- 2. Tariffs: the price of one vehicle type at one operator
CREATE TABLE tariffs (
    id              INT              NOT NULL AUTO_INCREMENT,
    operator_id     INT              NOT NULL,
    vehicle_type    TINYINT UNSIGNED NOT NULL,
    unlock_fee_kop  INT              NOT NULL,
    per_minute_kop  INT              NOT NULL,
    CONSTRAINT pk_tariffs PRIMARY KEY (id),
    CONSTRAINT uq_tariffs_operator_type UNIQUE (operator_id, vehicle_type),
    CONSTRAINT fk_tariffs_operator FOREIGN KEY (operator_id)
        REFERENCES operators (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_tariffs_vehicle_type CHECK (vehicle_type BETWEEN 1 AND 2),
    CONSTRAINT chk_tariffs_unlock_fee CHECK (unlock_fee_kop BETWEEN 0 AND 10000),
    CONSTRAINT chk_tariffs_per_minute CHECK (per_minute_kop BETWEEN 1 AND 5000)
) ENGINE = InnoDB;

-- 3. Stations: city parking places shared by all operators; an available vehicle waits at one of them
CREATE TABLE stations (
    id              INT              NOT NULL AUTO_INCREMENT,
    name            VARCHAR(60)      NOT NULL,
    address         VARCHAR(100)     NOT NULL,
    capacity        TINYINT UNSIGNED NOT NULL,
    CONSTRAINT pk_stations PRIMARY KEY (id),
    CONSTRAINT uq_stations_name UNIQUE (name),
    CONSTRAINT chk_stations_name CHECK (CHAR_LENGTH(TRIM(name)) >= 2),
    CONSTRAINT chk_stations_capacity CHECK (capacity BETWEEN 1 AND 50)
) ENGINE = InnoDB;

-- 4. Vehicles: one physical scooter or bike of an operator
CREATE TABLE vehicles (
    id              INT              NOT NULL AUTO_INCREMENT,
    code            VARCHAR(8)       NOT NULL,
    operator_id     INT              NOT NULL,
    vehicle_type    TINYINT UNSIGNED NOT NULL,
    model           VARCHAR(40)      NOT NULL,
    battery_pct     TINYINT UNSIGNED NOT NULL,
    status          TINYINT UNSIGNED NOT NULL DEFAULT 1,
    station_id      INT              NULL,
    CONSTRAINT pk_vehicles PRIMARY KEY (id),
    CONSTRAINT uq_vehicles_code UNIQUE (code),
    CONSTRAINT fk_vehicles_operator FOREIGN KEY (operator_id)
        REFERENCES operators (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    -- the operator has a tariff for this vehicle type (BR7)
    CONSTRAINT fk_vehicles_tariff FOREIGN KEY (operator_id, vehicle_type)
        REFERENCES tariffs (operator_id, vehicle_type) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_vehicles_station FOREIGN KEY (station_id)
        REFERENCES stations (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_vehicles_code CHECK (REGEXP_LIKE(code, '^[A-Z]{3}-[0-9]{4}$', 'c')),
    CONSTRAINT chk_vehicles_vehicle_type CHECK (vehicle_type BETWEEN 1 AND 2),
    CONSTRAINT chk_vehicles_battery CHECK (battery_pct <= 100),
    CONSTRAINT chk_vehicles_status CHECK (status BETWEEN 1 AND 5),
    -- a vehicle stands at a station exactly while it waits for a customer (status 1 AVAILABLE)
    CONSTRAINT chk_vehicles_station CHECK ((status = 1 AND station_id IS NOT NULL) OR (status <> 1 AND station_id IS NULL))
) ENGINE = InnoDB;
CREATE INDEX ix_vehicles_operator_type ON vehicles (operator_id, vehicle_type);
CREATE INDEX ix_vehicles_station ON vehicles (station_id);

-- 5. Customers: people who rent vehicles; one wallet each; identity verified through Diia (simulated)
CREATE TABLE customers (
    id              INT          NOT NULL AUTO_INCREMENT,
    last_name       VARCHAR(40)  NOT NULL,
    first_name      VARCHAR(40)  NOT NULL,
    phone           VARCHAR(13)  NOT NULL,
    email           VARCHAR(100) NULL,
    registered_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    diia_verified_at DATETIME    NULL,
    CONSTRAINT pk_customers PRIMARY KEY (id),
    CONSTRAINT uq_customers_phone UNIQUE (phone),
    CONSTRAINT uq_customers_email UNIQUE (email),
    CONSTRAINT chk_customers_names CHECK (CHAR_LENGTH(TRIM(last_name)) >= 2 AND CHAR_LENGTH(TRIM(first_name)) >= 2),
    CONSTRAINT chk_customers_phone CHECK (phone REGEXP '^\\+380[0-9]{9}$'),
    CONSTRAINT chk_customers_email CHECK (email IS NULL OR email LIKE '_%@_%._%'),
    CONSTRAINT chk_customers_verified CHECK (diia_verified_at IS NULL OR diia_verified_at >= registered_at)
) ENGINE = InnoDB;
CREATE INDEX ix_customers_last_name ON customers (last_name);

-- 6. Rides: one rental of one vehicle by one customer; ended_at IS NULL means the ride is active.
--    Prices are copied from the tariff at the start; the commission is fixed at the finish.
CREATE TABLE rides (
    id              INT          NOT NULL AUTO_INCREMENT,
    customer_id     INT          NOT NULL,
    vehicle_id      INT          NOT NULL,
    started_at      DATETIME     NOT NULL,
    ended_at        DATETIME     NULL,
    unlock_fee_kop  INT          NOT NULL,
    per_minute_kop  INT          NOT NULL,
    commission_kop  INT          NULL,
    CONSTRAINT pk_rides PRIMARY KEY (id),
    -- target of fk_payments_ride_customer: a payment names the ride together with the ride's customer
    CONSTRAINT uq_rides_id_customer UNIQUE (id, customer_id),
    CONSTRAINT fk_rides_customer FOREIGN KEY (customer_id)
        REFERENCES customers (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_rides_vehicle FOREIGN KEY (vehicle_id)
        REFERENCES vehicles (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    -- equal times are allowed: a finish in the same second is billed as one minute
    CONSTRAINT chk_rides_period CHECK (ended_at IS NULL OR ended_at >= started_at),
    CONSTRAINT chk_rides_unlock_fee CHECK (unlock_fee_kop BETWEEN 0 AND 10000),
    CONSTRAINT chk_rides_per_minute CHECK (per_minute_kop BETWEEN 1 AND 5000),
    -- the commission is fixed at the finish: none while active; 0 .. ride price when finished
    CONSTRAINT chk_rides_commission CHECK (
        (ended_at IS NULL AND commission_kop IS NULL)
        OR (ended_at IS NOT NULL AND commission_kop IS NOT NULL AND commission_kop BETWEEN 0 AND
            unlock_fee_kop + per_minute_kop * GREATEST(1, CEIL(TIMESTAMPDIFF(SECOND, started_at, ended_at) / 60))))
) ENGINE = InnoDB;
CREATE INDEX ix_rides_customer ON rides (customer_id, started_at);
CREATE INDEX ix_rides_vehicle ON rides (vehicle_id, started_at);
CREATE INDEX ix_rides_started_at ON rides (started_at);
CREATE INDEX ix_rides_ended_at ON rides (ended_at);
-- At most one active ride per customer and per vehicle (functional unique index; NULLs do not collide)
CREATE UNIQUE INDEX ux_rides_active_customer ON rides ((CASE WHEN ended_at IS NULL THEN customer_id END));
CREATE UNIQUE INDEX ux_rides_active_vehicle ON rides ((CASE WHEN ended_at IS NULL THEN vehicle_id END));

-- 7. Payments: the wallet ledger (1 bonus, 2 top-up, 3 ride charge, 4 ride refund); balance = SUM(amount_kop).
--    Rows are never edited: a correction is a new refund row.
CREATE TABLE payments (
    id              INT              NOT NULL AUTO_INCREMENT,
    customer_id     INT              NOT NULL,
    ride_id         INT              NULL,
    kind            TINYINT UNSIGNED NOT NULL,
    amount_kop      INT              NOT NULL,
    created_at      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_payments PRIMARY KEY (id),
    CONSTRAINT uq_payments_ride_kind UNIQUE (ride_id, kind),
    CONSTRAINT fk_payments_customer FOREIGN KEY (customer_id)
        REFERENCES customers (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    -- a charge or refund belongs to the ride's own customer (NULL ride_id of kinds 1, 2 is not checked)
    CONSTRAINT fk_payments_ride_customer FOREIGN KEY (ride_id, customer_id)
        REFERENCES rides (id, customer_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_payments_kind CHECK (kind BETWEEN 1 AND 4),
    -- 3 RIDE: a charge of one ride, negative; 4 REFUND: money back for one ride, positive;
    -- 1 BONUS and 2 TOPUP: money in, positive, no ride
    CONSTRAINT chk_payments_shape CHECK (
        (kind = 3 AND ride_id IS NOT NULL AND amount_kop < 0)
        OR (kind = 4 AND ride_id IS NOT NULL AND amount_kop > 0)
        OR (kind IN (1, 2) AND ride_id IS NULL AND amount_kop > 0)),
    -- a cap on money in (10 000 UAH); a charge is bounded by the price formula instead
    CONSTRAINT chk_payments_amount CHECK (amount_kop <= 1000000)
) ENGINE = InnoDB;
CREATE INDEX ix_payments_customer ON payments (customer_id, created_at);
CREATE INDEX ix_payments_ride_customer ON payments (ride_id, customer_id);

-- Negative and positive checks of the constraints. Run with: mysql --force micromobility < checks.sql
-- Every statement marked "expect error <name>" must fail with exactly that constraint (or MySQL error number);
-- run-test.sh compares the names in order; "expect ok" must pass;
-- every "expect 0" query must print 0.
-- Codes: vehicle_type 1 E_SCOOTER, 2 E_BIKE; vehicles.status 1 AVAILABLE, 2 IN_RIDE, 3 CHARGING, 4 REPAIR, 5 RETIRED;
-- payments.kind 1 BONUS, 2 TOPUP, 3 RIDE, 4 REFUND.
-- Wrapped in a transaction that is rolled back at the end, so the seed stays unchanged.
START TRANSACTION;
-- Ids taken from the seed, so the script does not depend on generated numbers
SET @active_customer = (SELECT customer_id FROM rides WHERE ended_at IS NULL ORDER BY id LIMIT 1);
SET @active_vehicle  = (SELECT vehicle_id FROM rides WHERE ended_at IS NULL ORDER BY id LIMIT 1);
SET @idle_customer   = (SELECT c.id FROM customers c WHERE NOT EXISTS (SELECT 1 FROM rides r WHERE r.customer_id = c.id) LIMIT 1);
SET @free_vehicle    = (SELECT v.id FROM vehicles v WHERE v.status = 1 AND v.battery_pct >= 10 AND NOT EXISTS (SELECT 1 FROM rides r WHERE r.vehicle_id = v.id AND r.ended_at IS NULL) ORDER BY v.id LIMIT 1);
SET @free_station    = (SELECT station_id FROM vehicles WHERE id = @free_vehicle);
SET @empty_station   = (SELECT s.id FROM stations s WHERE NOT EXISTS (SELECT 1 FROM vehicles v WHERE v.station_id = s.id) LIMIT 1);
SET @charged_ride    = (SELECT MIN(ride_id) FROM payments WHERE kind = 3);
SET @rider           = (SELECT customer_id FROM rides WHERE id = @charged_ride);
SET @other_customer  = (SELECT MIN(id) FROM customers WHERE id <> @rider);

-- C1 expect 0: BR21 consistency on the seed, status 2 (IN_RIDE) if and only if the vehicle has an active ride
SELECT COUNT(*) AS c1_status_mismatches FROM vehicles v
WHERE (v.status = 2) <> EXISTS (SELECT 1 FROM rides r WHERE r.vehicle_id = v.id AND r.ended_at IS NULL);
-- C3 expect 0: BR8 on the seed, every ride started with a balance of at least 100 UAH
SELECT COUNT(*) AS c3_unlock_below_100 FROM rides r
WHERE (SELECT COALESCE(SUM(p.amount_kop), 0) FROM payments p
       WHERE p.customer_id = r.customer_id AND p.created_at < r.started_at) < 10000;
-- C4 expect 0: BR22 on the seed, no station holds more vehicles than its capacity
SELECT COUNT(*) AS c4_stations_over_capacity FROM stations s
WHERE s.capacity < (SELECT COUNT(*) FROM vehicles v WHERE v.station_id = s.id);

-- N1 expect error chk_operators_phone
INSERT INTO operators (name, phone, email) VALUES ('Test', '0440000000', 'a@b.ua');
-- N2 expect error chk_operators_commission
INSERT INTO operators (name, phone, email, commission_pct) VALUES ('Test', '+380440000999', 'a@b.ua', 60);
-- N3 expect error uq_operators_name (case-insensitive: 'bolt' = 'Bolt')
INSERT INTO operators (name, phone, email) VALUES ('bolt', '+380440000999', 'a@b.ua');
-- N4 expect error 1264 (out of range; DECIMAL(4,2) holds at most 99.99; checked before the CHECK)
INSERT INTO operators (name, phone, email, commission_pct) VALUES ('Test', '+380440000999', 'a@b.ua', 100);
-- N5 expect error uq_tariffs_operator_type
INSERT INTO tariffs (operator_id, vehicle_type, unlock_fee_kop, per_minute_kop) VALUES (1, 1, 1000, 500);
-- N6 expect error chk_tariffs_per_minute
INSERT INTO tariffs (operator_id, vehicle_type, unlock_fee_kop, per_minute_kop) VALUES (2, 2, 1000, 0);
-- N7 expect error fk_tariffs_operator
INSERT INTO tariffs (operator_id, vehicle_type, unlock_fee_kop, per_minute_kop) VALUES (99, 2, 1000, 400);
-- N8 expect error chk_tariffs_vehicle_type (type code out of range)
INSERT INTO tariffs (operator_id, vehicle_type, unlock_fee_kop, per_minute_kop) VALUES (2, 3, 1000, 400);
-- N9 expect error chk_vehicles_code (lowercase is refused: case-sensitive match)
INSERT INTO vehicles (code, operator_id, vehicle_type, model, battery_pct, station_id) VALUES ('blt-0999', 1, 1, 'X', 50, @free_station);
-- N10 expect error chk_vehicles_battery
INSERT INTO vehicles (code, operator_id, vehicle_type, model, battery_pct, station_id) VALUES ('BLT-0999', 1, 1, 'X', 101, @free_station);
-- N11 expect error uq_vehicles_code
INSERT INTO vehicles (code, operator_id, vehicle_type, model, battery_pct, station_id) VALUES ('BLT-0001', 1, 1, 'X', 50, @free_station);
-- N12 expect error chk_vehicles_vehicle_type (type code out of range)
INSERT INTO vehicles (code, operator_id, vehicle_type, model, battery_pct, station_id) VALUES ('BLT-0999', 1, 0, 'X', 50, @free_station);
-- N13 expect error chk_vehicles_status (status code out of range)
INSERT INTO vehicles (code, operator_id, vehicle_type, model, battery_pct, status) VALUES ('BLT-0999', 1, 1, 'X', 50, 6);
-- N14 expect error chk_customers_phone
INSERT INTO customers (last_name, first_name, phone) VALUES ('Тест', 'Тест', '+38099');
-- N15 expect error uq_customers_phone
INSERT INTO customers (last_name, first_name, phone) VALUES ('Тест', 'Тест', '+380990000001');
-- N16 expect error chk_customers_email
INSERT INTO customers (last_name, first_name, phone, email) VALUES ('Тест', 'Тест', '+380990009999', 'no-at-sign');
-- N17 expect error chk_customers_names
INSERT INTO customers (last_name, first_name, phone) VALUES (' ', 'Тест', '+380990009999');
-- N18 expect error chk_rides_period (the finish before the start)
INSERT INTO rides (customer_id, vehicle_id, started_at, ended_at, unlock_fee_kop, per_minute_kop, commission_kop)
VALUES (@idle_customer, @free_vehicle, '2026-11-01 10:00:00', '2026-11-01 09:59:59', 1000, 500, 100);
-- N19 expect error ux_rides_active_customer (this customer already has an active ride)
INSERT INTO rides (customer_id, vehicle_id, started_at, unlock_fee_kop, per_minute_kop) VALUES (@active_customer, @free_vehicle, UTC_TIMESTAMP(), 1000, 500);
-- N20 expect error ux_rides_active_vehicle (this vehicle is already in an active ride)
INSERT INTO rides (customer_id, vehicle_id, started_at, unlock_fee_kop, per_minute_kop) VALUES (@idle_customer, @active_vehicle, UTC_TIMESTAMP(), 1000, 500);
-- N21 expect error chk_payments_shape (a top-up must be positive and have no ride)
INSERT INTO payments (customer_id, kind, amount_kop) VALUES (@rider, 2, -500);
-- N22 expect error chk_payments_shape (a ride charge needs its ride)
INSERT INTO payments (customer_id, kind, amount_kop) VALUES (@rider, 3, -500);
-- N23 expect error chk_payments_kind (kind code out of range)
INSERT INTO payments (customer_id, kind, amount_kop) VALUES (@rider, 5, 500);
-- N24 expect error uq_payments_ride_kind (one charge per ride)
INSERT INTO payments (customer_id, ride_id, kind, amount_kop) VALUES (@rider, @charged_ride, 3, -100);
-- N25 expect error fk_payments_customer (RESTRICT: a customer with rides and payments is not deleted; MySQL reports this FK first)
DELETE FROM customers WHERE id = @rider;
-- N26 expect error fk_tariffs_operator (RESTRICT: an operator with tariffs and vehicles is not deleted; MySQL reports this FK first)
DELETE FROM operators WHERE id = 1;
-- N27 expect error fk_payments_ride_customer (RESTRICT: a charged ride is not deleted)
DELETE FROM rides WHERE id = @charged_ride;
-- N28 expect error chk_payments_amount (money in above 10 000 UAH)
INSERT INTO payments (customer_id, kind, amount_kop) VALUES (@rider, 2, 2000000);
-- N29 expect error chk_customers_verified (verified before registration)
INSERT INTO customers (last_name, first_name, phone, registered_at, diia_verified_at)
VALUES ('Тест', 'Тест', '+380990009999', '2026-11-10 10:00:00', '2026-11-10 09:00:00');
-- N30 expect error chk_rides_commission (a finished ride without a commission)
INSERT INTO rides (customer_id, vehicle_id, started_at, ended_at, unlock_fee_kop, per_minute_kop)
VALUES (@idle_customer, @free_vehicle, '2026-11-01 10:00:00', '2026-11-01 10:10:00', 1000, 500);
-- N31 expect error chk_rides_commission (commission above the ride price 1000 + 10 * 500 = 6000)
INSERT INTO rides (customer_id, vehicle_id, started_at, ended_at, unlock_fee_kop, per_minute_kop, commission_kop)
VALUES (@idle_customer, @free_vehicle, '2026-11-01 10:00:00', '2026-11-01 10:10:00', 1000, 500, 6001);
-- N32 expect error chk_rides_commission (an active ride has no commission yet)
INSERT INTO rides (customer_id, vehicle_id, started_at, unlock_fee_kop, per_minute_kop, commission_kop)
VALUES (@idle_customer, @free_vehicle, UTC_TIMESTAMP(), 1000, 500, 100);
-- N33 expect error chk_payments_shape (a refund is positive and names its ride)
INSERT INTO payments (customer_id, ride_id, kind, amount_kop) VALUES (@rider, @charged_ride, 4, -100);
-- N34 expect error chk_payments_shape (a refund without a ride)
INSERT INTO payments (customer_id, kind, amount_kop) VALUES (@rider, 4, 100);
-- N36 expect error fk_payments_ride_customer (D27: a refund booked to another customer than the ride's)
INSERT INTO payments (customer_id, ride_id, kind, amount_kop) VALUES (@other_customer, @charged_ride, 4, 100);
-- N37 expect error fk_payments_customer (D28: RESTRICT; a customer with a bonus row is not deleted by the DB alone)
DELETE FROM customers WHERE id = @idle_customer;
-- N38 expect error chk_stations_capacity
INSERT INTO stations (name, address, capacity) VALUES ('Test Station', 'Test St, 1', 0);
-- N39 expect error uq_stations_name (case-insensitive)
INSERT INTO stations (name, address, capacity) VALUES ('khreshchatyk', 'Test St, 1', 5);
-- N40 expect error chk_stations_name
INSERT INTO stations (name, address, capacity) VALUES (' ', 'Test St, 1', 5);
-- N41 expect error chk_vehicles_station (an AVAILABLE vehicle must stand at a station)
INSERT INTO vehicles (code, operator_id, vehicle_type, model, battery_pct) VALUES ('BLT-0999', 1, 1, 'X', 50);
-- N42 expect error chk_vehicles_station (a vehicle in a ride is at no station)
UPDATE vehicles SET status = 2 WHERE id = @free_vehicle;
-- N43 expect error fk_vehicles_station (RESTRICT: a station with vehicles is not deleted)
DELETE FROM stations WHERE id = @free_station;
-- N44 expect error fk_vehicles_tariff (D29: Jet has no e-bike tariff, so a Jet e-bike cannot exist)
INSERT INTO vehicles (code, operator_id, vehicle_type, model, battery_pct, station_id) VALUES ('JET-0999', 2, 2, 'X', 50, @free_station);
-- N45 expect error fk_vehicles_tariff (D29: a tariff that vehicles use is not deleted)
DELETE FROM tariffs WHERE operator_id = 1 AND vehicle_type = 1;

-- P1 expect ok (the DB does not check Diia verification; the app does, BR8): a second customer starts a ride on a
-- free vehicle, finishes it at a station, and starts again; each step also sets status and station, as RideService does
INSERT INTO rides (customer_id, vehicle_id, started_at, unlock_fee_kop, per_minute_kop) VALUES (@idle_customer, @free_vehicle, UTC_TIMESTAMP() - INTERVAL 10 MINUTE, 1000, 500);
UPDATE vehicles SET status = 2, station_id = NULL WHERE id = @free_vehicle AND status = 1;
UPDATE rides SET ended_at = UTC_TIMESTAMP(), commission_kop = 600 WHERE customer_id = @idle_customer AND ended_at IS NULL;
UPDATE vehicles SET status = 1, station_id = @empty_station WHERE id = @free_vehicle AND status = 2;
INSERT INTO rides (customer_id, vehicle_id, started_at, unlock_fee_kop, per_minute_kop) VALUES (@idle_customer, @free_vehicle, UTC_TIMESTAMP(), 1000, 500);
UPDATE vehicles SET status = 2, station_id = NULL WHERE id = @free_vehicle AND status = 1;
-- P2 expect ok: an active ride without a charge can be deleted (cancel); the vehicle is AVAILABLE again at a station
DELETE FROM rides WHERE customer_id = @idle_customer AND ended_at IS NULL;
UPDATE vehicles SET status = 1, station_id = @free_station WHERE id = @free_vehicle AND status = 2;
-- C2 expect 0: the start, finish and cancel above kept BR21
SELECT COUNT(*) AS c2_status_mismatches FROM vehicles v
WHERE (v.status = 2) <> EXISTS (SELECT 1 FROM rides r WHERE r.vehicle_id = v.id AND r.ended_at IS NULL);
-- P3 expect ok (D28): the app deletes a customer without rides and without payments other than the bonus:
-- the bonus row first, then the customer, in one transaction
DELETE FROM rides WHERE customer_id = @idle_customer;
DELETE FROM payments WHERE customer_id = @idle_customer AND kind = 1;
DELETE FROM customers WHERE id = @idle_customer;
SELECT COUNT(*) AS payments_left_after_delete FROM payments WHERE customer_id = @idle_customer;
-- P4 expect ok: two NULL e-mails do not collide
SELECT COUNT(*) AS customers_without_email FROM customers WHERE email IS NULL;
-- P5 expect ok: a finished ride at exactly the price limit, then one refund for the first charged ride without a refund
INSERT INTO rides (customer_id, vehicle_id, started_at, ended_at, unlock_fee_kop, per_minute_kop, commission_kop)
VALUES (@rider, @free_vehicle, '2026-11-02 10:00:00', '2026-11-02 10:10:00', 1000, 500, 6000);
SET @unrefunded = (SELECT MIN(p.ride_id) FROM payments p WHERE p.kind = 3
                   AND NOT EXISTS (SELECT 1 FROM payments f WHERE f.ride_id = p.ride_id AND f.kind = 4));
SET @unrefunded_customer = (SELECT customer_id FROM rides WHERE id = @unrefunded);
INSERT INTO payments (customer_id, ride_id, kind, amount_kop) VALUES (@unrefunded_customer, @unrefunded, 4, 100);
-- N35 expect error uq_payments_ride_kind (at most one refund per ride)
INSERT INTO payments (customer_id, ride_id, kind, amount_kop) VALUES (@unrefunded_customer, @unrefunded, 4, 50);
-- P6 expect ok: DECIMAL(4,2) rounds a third decimal half away from zero on insert: 12.345 is stored as 12.35
INSERT INTO operators (name, phone, email, commission_pct) VALUES ('Rounding Test', '+380440000999', 'r@t.example', 12.345);
SELECT commission_pct AS p6_stored_pct FROM operators WHERE name = 'Rounding Test';
-- P7 expect ok (M1): a finish in the same second as the start is a 1-minute ride (1000 + 500 = 1500, commission 150)
INSERT INTO rides (customer_id, vehicle_id, started_at, ended_at, unlock_fee_kop, per_minute_kop, commission_kop)
VALUES (@rider, @free_vehicle, '2026-11-03 10:00:00', '2026-11-03 10:00:00', 1000, 500, 150);
SET @same_second_ride = LAST_INSERT_ID();
INSERT INTO payments (customer_id, ride_id, kind, amount_kop, created_at) VALUES (@rider, @same_second_ride, 3, -1500, '2026-11-03 10:00:00');
SELECT COUNT(*) AS p7_same_second_rides FROM rides WHERE id = @same_second_ride;
-- P8 expect ok (M2): a 3-day ride (4320 min x 520 + 1000 = 2 247 400 kop) can still be charged
INSERT INTO rides (customer_id, vehicle_id, started_at, ended_at, unlock_fee_kop, per_minute_kop, commission_kop)
VALUES (@rider, @free_vehicle, '2026-11-04 10:00:00', '2026-11-07 10:00:00', 1000, 520, 224740);
SET @long_ride = LAST_INSERT_ID();
INSERT INTO payments (customer_id, ride_id, kind, amount_kop, created_at) VALUES (@rider, @long_ride, 3, -2247400, '2026-11-07 10:00:00');
SELECT amount_kop AS p8_long_ride_charge FROM payments WHERE ride_id = @long_ride AND kind = 3;
-- P9 expect ok (D26): an empty station is deleted; capacity and address of another one are edited
DELETE FROM stations WHERE id = @empty_station AND NOT EXISTS (SELECT 1 FROM vehicles v WHERE v.station_id = @empty_station);
UPDATE stations SET capacity = 12, address = 'Khreshchatyk St, 24' WHERE name = 'Khreshchatyk';
SELECT COUNT(*) AS p9_stations_left FROM stations;
-- P10 expect ok (D29): tariff prices change freely; only the (operator, type) key is locked by vehicles
UPDATE tariffs SET per_minute_kop = 530 WHERE operator_id = 1 AND vehicle_type = 1;

ROLLBACK;

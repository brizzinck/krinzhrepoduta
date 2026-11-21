-- Report queries R1-R7 with sample parameters. Run with: mysql -t micromobility < queries.sql
-- NULL in a filter parameter means "no filter". Money is in kopecks.
-- Date filters (same rule in the app and here): the user types Kyiv calendar days, "from" is the start of that day and
-- "to" is inclusive, so the app binds UTC bounds [start of "from" day, start of the day after "to"). Times in the
-- database are UTC, Kyiv is UTC+2 in winter and UTC+3 in summer (DST ends 2026-10-25). So Kyiv 01.11 to 30.11 is
-- '2026-10-31 22:00:00' to '2026-11-30 22:00:00' in the SQL below. R3 groups by the month of the finish in Kyiv time,
-- CONVERT_TZ(ended_at, '+00:00', 'Europe/Kyiv') (the mysql:8.4 image loads the time zone tables).
-- (the tables are loaded by the image entrypoint at the first start of a volume; check.sh part 0 asserts it).
-- Coded columns are returned as numbers; the app maps them to Java enums and shows the Ukrainian label:
--   vehicle_type 1 E_SCOOTER, 2 E_BIKE; vehicles.status 1 AVAILABLE, 2 IN_RIDE, 3 CHARGING, 4 REPAIR, 5 RETIRED;
--   payments.kind 1 BONUS, 2 TOPUP, 3 RIDE, 4 REFUND.

-- R1. Available vehicles now: status AVAILABLE, at a station, battery >= max(10, filter), no active ride;
--     cheapest 10-minute ride first. NOT EXISTS re-checks the rides table, the source of truth for BR21.
SET @op = NULL, @type = 1, @station = NULL, @min_battery = 20;
SELECT v.code, s.name AS station, o.name AS operator, v.vehicle_type, v.model, v.battery_pct,
       tr.unlock_fee_kop, tr.per_minute_kop,
       tr.unlock_fee_kop + 10 * tr.per_minute_kop AS price_10_min_kop
FROM vehicles v
JOIN operators o      ON o.id = v.operator_id
JOIN stations s       ON s.id = v.station_id
JOIN tariffs tr       ON tr.operator_id = v.operator_id AND tr.vehicle_type = v.vehicle_type
WHERE v.status = 1
  AND v.battery_pct >= GREATEST(10, COALESCE(@min_battery, 10))
  AND (@op IS NULL OR v.operator_id = @op)
  AND (@type IS NULL OR v.vehicle_type = @type)
  AND (@station IS NULL OR v.station_id = @station)
  AND NOT EXISTS (SELECT 1 FROM rides r WHERE r.vehicle_id = v.id AND r.ended_at IS NULL)
ORDER BY price_10_min_kop, v.battery_pct DESC, v.code;

-- R2. Ride search: period, operator, type, customer (surname prefix or phone suffix), status; every filter optional.
--     In the app this is JPQL; the minutes come from Billing.minutes(start, end) in Java, the same code as the charge.
-- Kyiv days 01.11 to 30.11 as UTC bounds
SET @from = '2026-10-31 22:00:00', @to = '2026-11-30 22:00:00', @op = NULL, @type = NULL, @q = 'Ко', @status = NULL;
SELECT r.id AS ride_id, CONCAT(c.last_name, ' ', c.first_name) AS customer, v.code AS vehicle,
       o.name AS operator, v.vehicle_type, r.started_at, r.ended_at,
       CASE WHEN r.ended_at IS NULL THEN NULL
            ELSE GREATEST(1, CEIL(TIMESTAMPDIFF(SECOND, r.started_at, r.ended_at) / 60)) END AS minutes,
       -ch.amount_kop AS cost_kop, rf.amount_kop AS refund_kop, r.commission_kop
FROM rides r
JOIN customers c      ON c.id = r.customer_id
JOIN vehicles v       ON v.id = r.vehicle_id
JOIN operators o      ON o.id = v.operator_id
LEFT JOIN payments ch ON ch.ride_id = r.id AND ch.kind = 3
LEFT JOIN payments rf ON rf.ride_id = r.id AND rf.kind = 4
WHERE (@from IS NULL OR r.started_at >= @from) AND (@to IS NULL OR r.started_at < @to)
  AND (@op IS NULL OR v.operator_id = @op)
  AND (@type IS NULL OR v.vehicle_type = @type)
  AND (@q IS NULL OR c.last_name LIKE CONCAT(@q, '%') OR c.phone LIKE CONCAT('%', @q))
  AND (@status IS NULL OR (@status = 'ACTIVE' AND r.ended_at IS NULL)
                       OR (@status = 'FINISHED' AND r.ended_at IS NOT NULL))
ORDER BY r.started_at DESC, r.id DESC;

-- R3. Revenue, refunds and commission by month of the finish and operator (commission as stored at the finish).
--     Served by ix_rides_ended_at. In the app this is HQL: group by the year and month of convert_tz(r.endedAt), o.id, o.name.
-- Kyiv days 01.10 to 30.11 as UTC bounds (01.10 is summer time, UTC+3)
SET @from = '2026-09-30 21:00:00', @to = '2026-11-30 22:00:00';
SELECT DATE_FORMAT(CONVERT_TZ(r.ended_at, '+00:00', 'Europe/Kyiv'), '%Y-%m') AS month, o.name AS operator,
       COUNT(*) AS rides,
       -SUM(ch.amount_kop) AS charged_kop,
       COALESCE(SUM(rf.amount_kop), 0) AS refunded_kop,
       -SUM(ch.amount_kop) - COALESCE(SUM(rf.amount_kop), 0) AS net_kop,
       SUM(r.commission_kop) AS commission_kop
FROM rides r
JOIN payments ch      ON ch.ride_id = r.id AND ch.kind = 3
LEFT JOIN payments rf ON rf.ride_id = r.id AND rf.kind = 4
JOIN vehicles v       ON v.id = r.vehicle_id
JOIN operators o      ON o.id = v.operator_id
WHERE (@from IS NULL OR r.ended_at >= @from) AND (@to IS NULL OR r.ended_at < @to)
GROUP BY month, o.id, o.name
ORDER BY month, charged_kop DESC, o.id;

-- R4. Top 5 customers by net spend (charges minus refunds) of rides finished in a period (the same base as R3),
--     only those with at least @min_rides rides
-- Kyiv days 01.10 to 30.11 as UTC bounds
SET @from = '2026-09-30 21:00:00', @to = '2026-11-30 22:00:00', @min_rides = 3;
SELECT c.id AS customer_id, CONCAT(c.last_name, ' ', c.first_name) AS customer,
       COUNT(DISTINCT r.id) AS rides, -SUM(p.amount_kop) AS spent_kop,
       ROUND(-SUM(p.amount_kop) / COUNT(DISTINCT r.id)) AS avg_ride_kop
FROM payments p
JOIN rides r      ON r.id = p.ride_id
JOIN customers c  ON c.id = r.customer_id
WHERE p.kind IN (3, 4) AND (@from IS NULL OR r.ended_at >= @from) AND (@to IS NULL OR r.ended_at < @to)
GROUP BY c.id, c.last_name, c.first_name
HAVING COUNT(DISTINCT r.id) >= COALESCE(@min_rides, 1)
ORDER BY spent_kop DESC, c.id
LIMIT 5;

-- R5. Pivot: finished rides and average billed minutes, operators in rows, vehicle types (1 E_SCOOTER, 2 E_BIKE)
--     in columns, total row. The total row has operator NULL and is_total 1; the page shows the label for it.
-- Kyiv days 01.10 to 30.11 as UTC bounds (01.10 is summer time, UTC+3)
SET @from = '2026-09-30 21:00:00', @to = '2026-11-30 22:00:00';
SELECT o.name AS operator, GROUPING(o.name) AS is_total,
       SUM(v.vehicle_type = 1) AS scooter_rides,
       ROUND(AVG(CASE WHEN v.vehicle_type = 1
                 THEN GREATEST(1, CEIL(TIMESTAMPDIFF(SECOND, r.started_at, r.ended_at) / 60)) END), 1) AS scooter_avg_min,
       SUM(v.vehicle_type = 2) AS bike_rides,
       ROUND(AVG(CASE WHEN v.vehicle_type = 2
                 THEN GREATEST(1, CEIL(TIMESTAMPDIFF(SECOND, r.started_at, r.ended_at) / 60)) END), 1) AS bike_avg_min,
       COUNT(*) AS total_rides
FROM rides r
JOIN vehicles v       ON v.id = r.vehicle_id
JOIN operators o      ON o.id = v.operator_id
WHERE r.ended_at IS NOT NULL
  AND (@from IS NULL OR r.started_at >= @from) AND (@to IS NULL OR r.started_at < @to)
GROUP BY o.name WITH ROLLUP
ORDER BY GROUPING(o.name), o.name;

-- R6. Wallet balances below a threshold (debtors and those who cannot unlock: < 100 UAH)
SET @threshold = 10000;
SELECT c.id AS customer_id, CONCAT(c.last_name, ' ', c.first_name) AS customer, c.phone,
       COALESCE(SUM(p.amount_kop), 0) AS balance_kop,
       COALESCE(SUM(p.kind = 2), 0) AS topups
FROM customers c
LEFT JOIN payments p ON p.customer_id = c.id
GROUP BY c.id, c.last_name, c.first_name, c.phone
HAVING balance_kop < @threshold
ORDER BY balance_kop, c.id;

-- R7. Vehicles to charge: AVAILABLE (status 1), battery below a level (Spring Data derived method);
--     vehicles already CHARGING (3) are not listed
SET @level = 20;
SELECT v.code, v.model, v.battery_pct, v.operator_id, v.station_id
FROM vehicles v
WHERE v.status = 1 AND v.battery_pct < @level
ORDER BY v.battery_pct, v.code;

-- Seed check 1: every charge equals the billing rule (expect 0 rows)
SELECT p.id AS payment_id, p.amount_kop,
       -(r.unlock_fee_kop + r.per_minute_kop * GREATEST(1, CEIL(TIMESTAMPDIFF(SECOND, r.started_at, r.ended_at) / 60)))
           AS expected_kop
FROM payments p JOIN rides r ON r.id = p.ride_id
WHERE p.kind = 3
  AND p.amount_kop <> -(r.unlock_fee_kop + r.per_minute_kop * GREATEST(1, CEIL(TIMESTAMPDIFF(SECOND, r.started_at, r.ended_at) / 60)));

-- Seed check 2: every refund is at most its charge (expect 0 rows; the app guards this rule, BR20)
SELECT rf.id AS payment_id FROM payments rf JOIN payments ch ON ch.ride_id = rf.ride_id AND ch.kind = 3
WHERE rf.kind = 4 AND rf.amount_kop > -ch.amount_kop;

-- Seed check 3 (also a defence demo of the stored commission): rides whose commission differs from
-- the operator's CURRENT percentage. Expect only Zelektra rides finished before 2026-11-01 (10 % then, 12 % now).
SELECT o.name AS operator, COUNT(*) AS rides, MIN(r.ended_at) AS first_end, MAX(r.ended_at) AS last_end,
       SUM(r.commission_kop) AS stored_kop,
       SUM(ROUND(-ch.amount_kop * o.commission_pct / 100)) AS at_current_pct_kop
FROM rides r
JOIN payments ch ON ch.ride_id = r.id AND ch.kind = 3
JOIN vehicles v  ON v.id = r.vehicle_id
JOIN operators o ON o.id = v.operator_id
WHERE r.commission_kop <> ROUND(-ch.amount_kop * o.commission_pct / 100)
GROUP BY o.name;

-- Seed check 4 (BR21): status 2 IN_RIDE if and only if the vehicle has an active ride (expect 0 rows)
SELECT v.id, v.code, v.status FROM vehicles v
WHERE (v.status = 2) <> EXISTS (SELECT 1 FROM rides r WHERE r.vehicle_id = v.id AND r.ended_at IS NULL);

-- Seed check 5 (D23): vehicle status distribution (1 AVAILABLE, 2 IN_RIDE, 3 CHARGING, 4 REPAIR, 5 RETIRED)
SELECT status, COUNT(*) AS vehicles FROM vehicles GROUP BY status ORDER BY status;

-- Seed check 6 (BR8): every ride started with a wallet balance of at least 100 UAH (expect 0 rows)
SELECT r.id FROM rides r
WHERE (SELECT COALESCE(SUM(p.amount_kop), 0) FROM payments p
       WHERE p.customer_id = r.customer_id AND p.created_at < r.started_at) < 10000;

-- Seed check 7 (BR22): vehicles per station against the capacity (no row may have parked > capacity)
SELECT s.id, s.name, s.capacity, COUNT(v.id) AS parked
FROM stations s LEFT JOIN vehicles v ON v.station_id = s.id
GROUP BY s.id, s.name, s.capacity ORDER BY s.id;

-- Seed check 8 (BR16, n4): the retired vehicle keeps its ride history
SELECT v.code, v.status, COUNT(r.id) AS rides, MAX(r.ended_at) AS last_ride
FROM vehicles v JOIN rides r ON r.vehicle_id = v.id WHERE v.status = 5 GROUP BY v.code, v.status;

-- Seed check 9 (accepted copies d and f): a charge or refund names the ride's own customer (also guarded by
-- fk_payments_ride_customer) and a charge is dated at the ride's finish (expect 0 rows)
SELECT p.id FROM payments p JOIN rides r ON r.id = p.ride_id
WHERE p.customer_id <> r.customer_id OR (p.kind = 3 AND p.created_at <> r.ended_at);

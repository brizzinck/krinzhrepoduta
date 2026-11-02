SELECT car_id, plate_number, daily_price, status
FROM cars
WHERE status = 'available' AND daily_price > 1500;

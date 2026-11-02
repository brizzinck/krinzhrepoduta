SELECT r.rental_id, r.car_id, r.daily_price AS contract_price, c.daily_price AS current_price
FROM rentals r
JOIN cars c ON r.car_id = c.car_id AND r.daily_price < c.daily_price;

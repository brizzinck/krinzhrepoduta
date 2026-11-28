-- Крок 3. Масове підвищення ставки класу «Економ» на 10 %. «До» і «Після»: ставка до UPDATE і після.
SELECT name, daily_base_rate FROM car_classes WHERE name = 'Економ';
UPDATE car_classes SET daily_base_rate = ROUND(daily_base_rate * 1.10, 2) WHERE name = 'Економ';
SELECT name, daily_base_rate FROM car_classes WHERE name = 'Економ';

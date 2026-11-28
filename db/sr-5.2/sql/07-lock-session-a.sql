-- Крок 2. Блокування, сеанс A: бере блокування рядка car_id = 1 і тримає його 10 секунд, потім ROLLBACK (дані не змінюються).
-- Звідки: demo.sh, функція lock(). Запускається у фоні в базі car_rental.
START TRANSACTION;
UPDATE cars SET mileage_km = mileage_km WHERE car_id = 1;
DO SLEEP(10);
ROLLBACK;

-- Крок 1. Кількість рядків у кожній з 10 таблиць бази car_rental.
SELECT 'branches' AS tbl, COUNT(*) AS n FROM branches
UNION ALL SELECT 'car_classes', COUNT(*) FROM car_classes
UNION ALL SELECT 'car_models', COUNT(*) FROM car_models
UNION ALL SELECT 'cars', COUNT(*) FROM cars
UNION ALL SELECT 'customers', COUNT(*) FROM customers
UNION ALL SELECT 'employees', COUNT(*) FROM employees
UNION ALL SELECT 'rentals', COUNT(*) FROM rentals
UNION ALL SELECT 'payments', COUNT(*) FROM payments
UNION ALL SELECT 'extras', COUNT(*) FROM extras
UNION ALL SELECT 'rental_extras', COUNT(*) FROM rental_extras;

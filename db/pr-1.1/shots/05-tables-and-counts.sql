SELECT 'branches' AS table_name, COUNT(*) AS row_count FROM branches UNION ALL
SELECT 'car_classes' AS table_name, COUNT(*) AS row_count FROM car_classes UNION ALL
SELECT 'car_models' AS table_name, COUNT(*) AS row_count FROM car_models UNION ALL
SELECT 'cars' AS table_name, COUNT(*) AS row_count FROM cars UNION ALL
SELECT 'customers' AS table_name, COUNT(*) AS row_count FROM customers UNION ALL
SELECT 'employees' AS table_name, COUNT(*) AS row_count FROM employees UNION ALL
SELECT 'rentals' AS table_name, COUNT(*) AS row_count FROM rentals UNION ALL
SELECT 'payments' AS table_name, COUNT(*) AS row_count FROM payments UNION ALL
SELECT 'extras' AS table_name, COUNT(*) AS row_count FROM extras UNION ALL
SELECT 'rental_extras' AS table_name, COUNT(*) AS row_count FROM rental_extras;

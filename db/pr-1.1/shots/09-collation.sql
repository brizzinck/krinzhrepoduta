SELECT customer_id, last_name FROM customers WHERE last_name = 'КОВАЛЕНКО';
SELECT first_name, last_name FROM customers ORDER BY first_name LIMIT 8;
SELECT table_name, column_name, character_set_name, collation_name FROM information_schema.columns WHERE table_schema = 'car_rental' AND table_name = 'customers' AND data_type = 'varchar' LIMIT 4;

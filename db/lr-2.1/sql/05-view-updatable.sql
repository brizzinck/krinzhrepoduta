-- Крок 5. Просте представлення оновлюване, а з групуванням ні (помилка 1288)
CREATE VIEW v_german_customers AS SELECT CustomerID, CompanyName, City FROM customers WHERE Country = 'Germany';
UPDATE v_german_customers SET City = 'Berlin' WHERE CustomerID = 'ALFKI';
CREATE VIEW v_city_counts AS SELECT City, COUNT(*) AS cnt FROM customers GROUP BY City;
UPDATE v_city_counts SET cnt = 0 WHERE City = 'Berlin';

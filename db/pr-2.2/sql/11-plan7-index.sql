-- Крок 11. Запит 7: EXPLAIN до і після індексу (Country, CompanyName).
-- До: без індексу, повний перегляд (type ALL, rows 29, Using where).
EXPLAIN SELECT CompanyName FROM suppliers WHERE Country='Germany';

-- Зміна: покриваючий індекс по (Country, CompanyName).
CREATE INDEX idx_suppliers_country ON suppliers (Country, CompanyName);

-- Після: запит той самий, читається індекс (type ref, key idx_suppliers_country, rows 3, Using index).
EXPLAIN SELECT CompanyName FROM suppliers WHERE Country='Germany';

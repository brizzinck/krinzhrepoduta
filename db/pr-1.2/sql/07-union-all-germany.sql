-- Запит 7. Постачальники й клієнти з Німеччини без видалення дублікатів (UNION ALL), 14 рядків.
SELECT CompanyName, 'Supplier' AS role FROM suppliers WHERE Country = 'Germany'
UNION ALL
SELECT CompanyName, 'Customer' FROM customers WHERE Country = 'Germany';

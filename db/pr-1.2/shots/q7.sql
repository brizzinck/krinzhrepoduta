SELECT CompanyName, 'Supplier' AS role FROM suppliers WHERE Country = 'Germany'
UNION ALL
SELECT CompanyName, 'Customer' FROM customers WHERE Country = 'Germany';

SELECT COUNT(*) AS rows_union_all FROM (
SELECT CompanyName, 'Supplier' AS role FROM suppliers WHERE Country = 'Germany'
UNION ALL
SELECT CompanyName, 'Customer' FROM customers WHERE Country = 'Germany'
) AS t;

-- для порівняння: UNION видаляє дублікати
SELECT COUNT(*) AS rows_union FROM (
SELECT CompanyName, 'Supplier' AS role FROM suppliers WHERE Country = 'Germany'
UNION
SELECT CompanyName, 'Customer' FROM customers WHERE Country = 'Germany'
) AS t;

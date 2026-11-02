SELECT b.city, e.name AS extra FROM branches b, extras e;

SELECT COUNT(*) AS rows_comma FROM branches b, extras e;
SELECT COUNT(*) AS rows_cross_join FROM branches b CROSS JOIN extras e;

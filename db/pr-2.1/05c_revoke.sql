-- Відкликаємо привілей CREATE (у MySQL немає DENY: відсутній привілей = заборона)
REVOKE CREATE ON northwind.* FROM 'LavrishkoVladyslavTCR32'@'%';
SHOW GRANTS FOR 'LavrishkoVladyslavTCR32'@'%';

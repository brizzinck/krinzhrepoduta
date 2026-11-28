-- Крок 6. Роль замість окремих привілеїв
CREATE ROLE 'nw_reader';
GRANT SELECT ON northwind_defense.* TO 'nw_reader';
CREATE USER 'lavrishko_ro'@'%' IDENTIFIED BY 'Lavr_dev_2026';
GRANT 'nw_reader' TO 'lavrishko_ro'@'%';
SHOW GRANTS FOR 'lavrishko_ro'@'%' USING 'nw_reader';
DROP USER 'lavrishko_ro'@'%';
DROP ROLE 'nw_reader';

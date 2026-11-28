-- Крок 0 (методичка, крок 00). Службові процедури-цикли (аналог WHILE із методички): fill_seq, fill_fmt, fill_uuid, fill_rand, probe_next_page.
-- Прибирає таблицю test_index зі старого запуску. Виконує make db, вивід не показується.
DROP TABLE IF EXISTS test_index;
DROP TABLE IF EXISTS test_index_nu;
DROP TABLE IF EXISTS test_index_u;
DROP PROCEDURE IF EXISTS fill_seq;
DROP PROCEDURE IF EXISTS fill_fmt;
DROP PROCEDURE IF EXISTS fill_uuid;
DROP PROCEDURE IF EXISTS fill_rand;
DROP PROCEDURE IF EXISTS probe_next_page;
DELIMITER $$
CREATE PROCEDURE fill_seq(IN from_id INT, IN to_id INT)
BEGIN
  DECLARE i INT DEFAULT from_id;
  START TRANSACTION;
  WHILE i <= to_id DO
    INSERT INTO test_index VALUES (i, ' a', 'b ');
    SET i = i + 1;
  END WHILE;
  COMMIT;
END$$
CREATE PROCEDURE fill_fmt(IN n INT, IN constant TINYINT)
BEGIN
  DECLARE i INT DEFAULT 0;
  START TRANSACTION;
  WHILE i < n DO
    SET i = i + 1;
    INSERT INTO test_index VALUES (i, IF(constant = 1, CAST(n AS CHAR), IF(i < 1000, LPAD(i, 4, '0'), CAST(i AS CHAR))), 'b');
  END WHILE;
  COMMIT;
END$$
CREATE PROCEDURE fill_uuid(IN n INT)
BEGIN
  DECLARE i INT DEFAULT 0;
  START TRANSACTION;
  WHILE i < n DO
    SET i = i + 1;
    INSERT INTO test_index VALUES (i, UUID(), 'b');
  END WHILE;
  COMMIT;
END$$
CREATE PROCEDURE fill_rand(IN n INT)
BEGIN
  DECLARE i INT DEFAULT 0;
  START TRANSACTION;
  WHILE i < n DO
    SET i = i + 1;
    INSERT INTO test_index VALUES (i, LOWER(HEX(RANDOM_BYTES(18))), 'b');
  END WHILE;
  COMMIT;
END$$
CREATE PROCEDURE probe_next_page()
BEGIN
  DECLARE n INT;
  DECLARE p0 INT;
  DECLARE p1 INT;
  SELECT COUNT(*) INTO n FROM test_index;
  SET p0 = (SELECT COUNT(*) FROM information_schema.INNODB_BUFFER_PAGE WHERE TABLE_NAME = '`northwind_opt`.`test_index`' AND PAGE_TYPE = 'INDEX');
  SET p1 = p0;
  WHILE p1 = p0 DO
    SET n = n + 1;
    INSERT INTO test_index VALUES (n, ' a', 'b ');
    SET p1 = (SELECT COUNT(*) FROM information_schema.INNODB_BUFFER_PAGE WHERE TABLE_NAME = '`northwind_opt`.`test_index`' AND PAGE_TYPE = 'INDEX');
  END WHILE;
  SELECT n AS new_page_row, p0 AS pages_before, p1 AS pages_after;
END$$
DELIMITER ;


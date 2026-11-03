-- ПР 2.2, завдання 1: сторінки й індекси на таблиці test_index (MySQL 8.4, InnoDB, база northwind_opt).
-- Після кожного кроку знято статистику сторінок (ibdstat.py) і обсяги (ANALYZE TABLE та запит нижче).
USE northwind_opt;

-- Аналог sp_spaceused
-- SELECT TABLE_NAME AS name, TABLE_ROWS AS `rows`, (SELECT COUNT(*) FROM test_index) AS exact_rows,
--        ROUND((DATA_LENGTH + INDEX_LENGTH) / 1024) AS reserved_kb, ROUND(DATA_LENGTH / 1024) AS data_kb,
--        ROUND(INDEX_LENGTH / 1024) AS index_kb, ROUND(DATA_FREE / 1024) AS unused_kb
-- FROM information_schema.TABLES WHERE TABLE_SCHEMA = 'northwind_opt' AND TABLE_NAME = 'test_index';

-- Аналог sys.indexes
-- SELECT t.NAME AS table_name, i.NAME AS index_name, i.TYPE AS type,
--        CASE WHEN i.NAME = 'GEN_CLUST_INDEX' THEN 'CLUSTERED (прихований)'
--             WHEN i.NAME = 'PRIMARY' THEN 'CLUSTERED (PRIMARY KEY)'
--             ELSE 'NONCLUSTERED' END AS type_desc
-- FROM information_schema.INNODB_INDEXES i
-- JOIN information_schema.INNODB_TABLES t USING (TABLE_ID)
-- WHERE t.NAME = 'northwind_opt/test_index';

-- 00. Службові процедури-цикли (аналог WHILE із методички)
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

-- 01. створення таблиці без ключа, порожня
CREATE TABLE test_index (
  id INT NOT NULL,
  pole1 CHAR(36) CHARACTER SET ascii NOT NULL,
  pole2 CHAR(216) CHARACTER SET ascii NOT NULL
);

-- 02. 1 рядок
INSERT INTO test_index VALUES (1, ' a', 'b ');

-- 03. 30 рядків (як у методичці)
CALL fill_seq(2, 30);

-- 04. рядок, що виділяє наступну сторінку
CALL probe_next_page();

-- 05. заповнення до 240 рядків
SET @n = (SELECT COUNT(*) FROM test_index);
CALL fill_seq(@n + 1, 240);

-- 06. ще один рядок (241-й)
INSERT INTO test_index VALUES (241, ' a', 'b ');

-- 07. кластеризований індекс (PRIMARY KEY)
TRUNCATE TABLE test_index;
ALTER TABLE test_index ADD PRIMARY KEY (id);

-- 08. 18630 унікальних послідовних ключів
CALL fill_seq(1, 18630);

-- 09. 18631-й рядок
INSERT INTO test_index VALUES (18631, ' a', 'b ');

-- 11. дубль кластеризованого ключа
TRUNCATE TABLE test_index;
CALL fill_seq(1, 5);
INSERT INTO test_index VALUES (3, ' a', 'b ');

-- 11b. неунікальний вторинний індекс, 8906 рядків (j % 100)
DROP TABLE IF EXISTS test_index_nu;
CREATE TABLE test_index_nu (id INT NOT NULL, pole1 CHAR(36) CHARACTER SET ascii NOT NULL, pole2 CHAR(216) CHARACTER SET ascii NOT NULL, KEY idx_id (id));
SET SESSION cte_max_recursion_depth = 20000;
INSERT INTO test_index_nu WITH RECURSIVE s(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM s WHERE n < 8906) SELECT n % 100, ' a', 'b ' FROM s;

-- 11c. ще один рядок і порівняння з унікальними значеннями
INSERT INTO test_index_nu VALUES (8907 % 100, ' a', 'b ');
DROP TABLE IF EXISTS test_index_u;
CREATE TABLE test_index_u LIKE test_index_nu;
SET SESSION cte_max_recursion_depth = 20000;
INSERT INTO test_index_u WITH RECURSIVE s(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM s WHERE n < 8907) SELECT n, ' a', 'b ' FROM s;

-- 12. кластеризований ключ pole1, послідовні значення, 9000 рядків
DROP TABLE test_index_nu;
DROP TABLE test_index_u;
TRUNCATE TABLE test_index;
ALTER TABLE test_index DROP PRIMARY KEY, ADD PRIMARY KEY (pole1);
CALL fill_fmt(9000, 0);

-- 13. кластеризований ключ pole1 = UUID(), 9000 рядків
TRUNCATE TABLE test_index;
CALL fill_uuid(9000);
SELECT pole1, id FROM test_index ORDER BY id LIMIT 2;

-- 14. кластеризований ключ pole1 = RANDOM_BYTES, 9000 рядків
TRUNCATE TABLE test_index;
CALL fill_rand(9000);
SELECT pole1, id FROM test_index ORDER BY id LIMIT 2;

-- 15. перебудова таблиці (OPTIMIZE TABLE)
OPTIMIZE TABLE test_index;

-- 16. некластеризований індекс на таблиці без PK, 24472 рядки
TRUNCATE TABLE test_index;
ALTER TABLE test_index DROP PRIMARY KEY;
CREATE INDEX idx_ncl_pole1 ON test_index (pole1);
CALL fill_fmt(24472, 0);

-- 17. ще один рядок (24473)
INSERT INTO test_index VALUES (24473, ' 000024473', 'b ');

-- 18. кластеризований PK(id) і некластеризований idx_ncl_pole1, 28864 рядки
DROP INDEX idx_ncl_pole1 ON test_index;
TRUNCATE TABLE test_index;
ALTER TABLE test_index ADD PRIMARY KEY (id);
CREATE INDEX idx_ncl_pole1 ON test_index (pole1);
CALL fill_fmt(28864, 1);
INSERT INTO test_index VALUES (28865, ' 000028865', 'b ');

-- 19. перебудова обох індексів (OPTIMIZE TABLE)
OPTIMIZE TABLE test_index;


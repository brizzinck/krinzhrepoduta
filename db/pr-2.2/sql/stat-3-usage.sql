-- Статистика сторінок, крок 3: обсяги таблиці (аналог sp_spaceused).
ANALYZE TABLE test_index;
SELECT TABLE_NAME AS name, TABLE_ROWS AS `rows`, (SELECT COUNT(*) FROM test_index) AS exact_rows,
    ROUND((DATA_LENGTH + INDEX_LENGTH) / 1024) AS reserved_kb, ROUND(DATA_LENGTH / 1024) AS data_kb,
    ROUND(INDEX_LENGTH / 1024) AS index_kb, ROUND(DATA_FREE / 1024) AS unused_kb
  FROM information_schema.TABLES WHERE TABLE_SCHEMA = 'northwind_opt' AND TABLE_NAME = 'test_index';

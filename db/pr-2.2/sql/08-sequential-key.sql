-- Крок 8 (методичка, крок 12). Кластеризований ключ pole1 з послідовними значеннями, 9000 рядків.
-- DROP TABLE IF EXISTS прибирає службові таблиці з кроку 11b, яких у цьому показі немає (без помилки 1051).
DROP TABLE IF EXISTS test_index_nu;
DROP TABLE IF EXISTS test_index_u;
TRUNCATE TABLE test_index;
ALTER TABLE test_index DROP PRIMARY KEY, ADD PRIMARY KEY (pole1);
CALL fill_fmt(9000, 0);


-- Крок 9 (методичка, крок 14). Кластеризований ключ pole1 з випадкових байтів, 9000 рядків: фрагментація.
TRUNCATE TABLE test_index;
CALL fill_rand(9000);
SELECT pole1, id FROM test_index ORDER BY id LIMIT 2;


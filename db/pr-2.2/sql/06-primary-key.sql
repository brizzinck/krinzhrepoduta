-- Крок 6 (методичка, кроки 07-08). Кластеризований індекс PRIMARY KEY, потім 18630 послідовних ключів.
TRUNCATE TABLE test_index;
ALTER TABLE test_index ADD PRIMARY KEY (id);
CALL fill_seq(1, 18630);


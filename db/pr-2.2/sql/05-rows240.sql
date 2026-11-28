-- Крок 5 (методичка, крок 05). Дозаповнення таблиці до 240 рядків.
SET @n = (SELECT COUNT(*) FROM test_index);
CALL fill_seq(@n + 1, 240);


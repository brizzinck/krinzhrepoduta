-- Статистика сторінок, крок 1: скинути сторінки test_index на диск і скопіювати .ibd, поки таблицю заблоковано.
-- Команда \! виконується всередині контейнера.
FLUSH TABLES test_index FOR EXPORT;
\! cp /var/lib/mysql/northwind_opt/test_index.ibd /tmp/test_index.ibd
UNLOCK TABLES;

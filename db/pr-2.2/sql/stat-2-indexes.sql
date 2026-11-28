-- Статистика сторінок, крок 2: назва індексу, його номер і коренева сторінка. Результат іде в ibdstat.py.
SELECT CONCAT(i.NAME, '=', i.INDEX_ID, '@', i.PAGE_NO)
  FROM information_schema.INNODB_INDEXES i JOIN information_schema.INNODB_TABLES t USING (TABLE_ID)
  WHERE t.NAME = 'northwind_opt/test_index' ORDER BY i.INDEX_ID;

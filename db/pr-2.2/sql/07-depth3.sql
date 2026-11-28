-- Крок 7 (методичка, крок 10b). 66138 рядків: дерево індексу отримує третій рівень.
TRUNCATE TABLE test_index;
SET SESSION cte_max_recursion_depth = 1000000;
INSERT INTO test_index WITH RECURSIVE s(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM s WHERE n < 66138) SELECT n, ' a', 'b ' FROM s;


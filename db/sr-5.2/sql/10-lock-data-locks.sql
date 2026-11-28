/* Крок 2. Блокування, сеанс C: самі блокування рядка (X,REC_NOT_GAP GRANTED у сеансу A і WAITING у сеансу B).
   Звідки: demo.sh, функція lock(). */
SELECT object_name, lock_type, lock_mode, lock_status, lock_data
FROM performance_schema.data_locks
WHERE object_name = 'cars';

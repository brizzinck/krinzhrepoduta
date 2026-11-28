/* Крок 2. Блокування, сеанс C: хто кого чекає. Очікувано: waiting_query UPDATE cars SET status ..., blocking_query DO SLEEP(10).
   Звідки: demo.sh, функція lock(). Запускається через 2 секунди після сеансу B, поки A ще тримає блокування. */
SELECT locked_table, wait_age_secs, waiting_pid, waiting_query, blocking_pid, blocking_query
FROM sys.innodb_lock_waits;

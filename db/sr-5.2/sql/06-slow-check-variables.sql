/* Крок 1. Перевірка: налаштування повернуто (slow_query_log OFF, long_query_time 10.000000, log_output FILE).
   Звідки: demo.sh, функція slow(). */
SHOW GLOBAL VARIABLES WHERE Variable_name IN ('slow_query_log', 'long_query_time', 'log_output');

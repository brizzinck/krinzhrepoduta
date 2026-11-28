/* Крок 1. Повільний журнал: вмикаємо запис у таблицю mysql.slow_log з порогом 0 (фіксується кожен запит).
   Звідки: demo.sh, функція slow(). Виконується від root без вибору бази. Налаштування рівня сервера повертає 05-slow-restore.sql. */
SET GLOBAL log_output = 'TABLE';
SET GLOBAL long_query_time = 0;
SET GLOBAL slow_query_log = ON;
TRUNCATE mysql.slow_log;
TRUNCATE performance_schema.events_statements_summary_by_digest;

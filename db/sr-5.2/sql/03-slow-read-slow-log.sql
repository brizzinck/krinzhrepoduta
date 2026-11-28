/* Крок 1. Читаємо повільний журнал: три рядки від запиту 02-slow-workload.sql.
   Звідки: demo.sh, функція slow(). */
SELECT query_time, rows_sent, rows_examined, LEFT(sql_text, 20) AS sql_text
FROM mysql.slow_log
WHERE db = 'car_rental' AND sql_text LIKE 'SELECT r.rental_id%';

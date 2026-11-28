/* Крок 4. Журнал помилок: запис про невдалий вхід (prio Note, error_code MY-010926, Access denied for user 'root'@'localhost').
   Звідки: demo.sh, функція errlog(). Час початку скрипт бере з 16-errlog-start-time.sql і підставляє через sed. */
SELECT logged, prio, error_code, LEFT(data, 62) AS data
FROM performance_schema.error_log
WHERE error_code = 'MY-010926' AND logged > '@T0@';

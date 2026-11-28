/* Крок 4. Журнал помилок: скільки записів про невдалий вхід (MY-010926) з'явилося після T0. При verbosity 2 це 0.
   Звідки: demo.sh, функція errlog(). Час початку скрипт бере з 16-errlog-start-time.sql і підставляє через sed. */
SELECT COUNT(*) AS login_errors
FROM performance_schema.error_log
WHERE error_code = 'MY-010926' AND logged > '@T0@';

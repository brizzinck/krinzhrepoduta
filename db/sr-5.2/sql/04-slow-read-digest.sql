/* Крок 1. Digest у performance_schema: той самий запит, згорнутий в один рядок.
   Звідки: demo.sh, функція slow(). У DIGEST_TEXT значення прибрано, ідентифікатори в зворотних лапках. */
SELECT LEFT(DIGEST_TEXT, 20) AS statement, COUNT_STAR AS cnt,
       ROUND(AVG_TIMER_WAIT / 1e9, 3) AS avg_ms, SUM_ROWS_EXAMINED AS rows_examined
FROM performance_schema.events_statements_summary_by_digest
WHERE SCHEMA_NAME = 'car_rental' AND DIGEST_TEXT LIKE 'SELECT `r` . `rental_id`%';

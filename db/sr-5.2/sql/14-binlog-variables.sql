/* Крок 3. Двійковий журнал: формат запису і чи ввімкнено журнал (binlog_format ROW, log_bin ON).
   Звідки: demo.sh, функція binlog(). */
SHOW VARIABLES WHERE Variable_name IN ('log_bin', 'binlog_format');

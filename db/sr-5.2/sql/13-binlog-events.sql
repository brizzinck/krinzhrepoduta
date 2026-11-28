/* Крок 3. Двійковий журнал: події після зміни (Anonymous_Gtid, Query BEGIN, Table_map, Update_rows, Xid).
   Звідки: demo.sh, функція binlog(). SHOW BINLOG EVENTS не приймає змінних, тому скрипт підставляє через sed ім'я файла і позицію (мітки в запиті нижче). Позиція з 11-binlog-status.sql. */
SHOW BINLOG EVENTS IN '@LOG@' FROM @POS@;

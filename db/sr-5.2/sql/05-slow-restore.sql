/* Крок 1. Повернення налаштувань повільного журналу до початкових значень сервера.
   Звідки: demo.sh, функція slow(), кінець. */
SET GLOBAL slow_query_log = OFF;
SET GLOBAL long_query_time = 10;
SET GLOBAL log_output = 'FILE';

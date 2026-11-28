/* Крок 2 (make modes). Чи вимагає сервер TLS від клієнтів. Локально OFF, в Azure ON.
   Звідки: demo/tls-modes.sh. Перша команда кроку 2 заходить по TLS і цим кладе вхід root у кеш сервера. */
SHOW VARIABLES LIKE 'require_secure_transport';

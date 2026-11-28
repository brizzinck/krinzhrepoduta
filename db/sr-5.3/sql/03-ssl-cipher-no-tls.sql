/* Крок 2 (make modes). Шифр поточного з'єднання. Запускається з --ssl-mode=DISABLED, тому очікувано порожній Ssl_cipher:
   з'єднання без шифрування. Звідки: demo/tls-modes.sh. */
SHOW SESSION STATUS LIKE 'Ssl_cipher';

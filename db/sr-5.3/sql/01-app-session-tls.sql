/* Крок 1 (make tls). Шифр і версія TLS з'єднання, яке тримає застосунок ПР 3.1 (користувач rental, sslMode=REQUIRED).
   Звідки: demo/tls-app.sh. Виконується від root, поки застосунок утримує з'єднання 8 секунд.
   Очікувано: Ssl_cipher TLS_AES_256_GCM_SHA384 і Ssl_version TLSv1.3. */
SELECT t.PROCESSLIST_USER AS user, s.VARIABLE_NAME AS variable, s.VARIABLE_VALUE AS value
FROM performance_schema.threads t
JOIN performance_schema.status_by_thread s ON s.THREAD_ID = t.THREAD_ID
WHERE t.PROCESSLIST_USER = 'rental' AND s.VARIABLE_NAME IN ('Ssl_version', 'Ssl_cipher');

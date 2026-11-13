-- Шифр і версія TLS зєднання, яке тримає застосунок (користувач rental)
SELECT t.PROCESSLIST_USER AS user, s.VARIABLE_NAME AS variable, s.VARIABLE_VALUE AS value
FROM performance_schema.threads t
JOIN performance_schema.status_by_thread s ON s.THREAD_ID = t.THREAD_ID
WHERE t.PROCESSLIST_USER = 'rental' AND s.VARIABLE_NAME IN ('Ssl_version', 'Ssl_cipher');

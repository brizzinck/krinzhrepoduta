-- Видалення користувача й перевірка; тестову таблицю прибирає адміністратор
DROP USER 'LavrishkoVladyslavTCR32'@'%';
SELECT COUNT(*) AS users_left FROM mysql.user WHERE user LIKE 'Lavrishko%';
DROP TABLE northwind.user_test;

-- Завдання 5. Користувач ПрізвищеІм'яГрупа (латиницею): LavrishkoVladyslavTCR32.
-- Пароль тестовий, лише для локального контейнера.
CREATE USER 'LavrishkoVladyslavTCR32'@'%' IDENTIFIED BY 'Lavr_dev_2026';
GRANT CREATE, INSERT, UPDATE, DELETE, SELECT ON northwind.* TO 'LavrishkoVladyslavTCR32'@'%';
SHOW GRANTS FOR 'LavrishkoVladyslavTCR32'@'%';

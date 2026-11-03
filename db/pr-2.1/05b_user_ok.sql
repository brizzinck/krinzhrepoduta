-- Під користувачем LavrishkoVladyslavTCR32: усі п'ять привілеїв працюють
CREATE TABLE user_test (id INT PRIMARY KEY, note VARCHAR(40));
INSERT INTO user_test VALUES (1, 'first'), (2, 'second');
UPDATE user_test SET note = 'changed' WHERE id = 2;
DELETE FROM user_test WHERE id = 1;
SELECT * FROM user_test;

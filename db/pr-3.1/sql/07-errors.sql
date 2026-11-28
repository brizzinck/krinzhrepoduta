-- Помилки бази: сценарій demo/05-errors.txt. Звідки: src/main/java/rental/TableDao.java, методи insert і delete;
-- помилки перехоплює Main.java, метод describe(SQLException). Програма друкує повідомлення й не падає.

-- 1. Помилка 1062 (UNIQUE): назва філії вже є. Значення: 'Прокат «Київ-Центр»', 'Київ', 'вул. Нова, 1', '+380440000000'.
INSERT INTO branches (name, city, address, phone) VALUES (?, ?, ?, ?);

-- 2. Помилка 3819 (CHECK): від'ємна ціна. Значення: 'Тестовий', '-5', '100'.
INSERT INTO car_classes (name, daily_base_rate, deposit_amount) VALUES (?, ?, ?);

-- 3. Помилка 1451 (FOREIGN KEY, ON DELETE RESTRICT): на філію 1 посилаються авто. Ключ: 1.
DELETE FROM branches WHERE branch_id = ?;

-- 4. Помилка 1364 (NOT NULL): обов'язкове поле name не заповнене, надіслано лише ціну. Значення: '10'.
INSERT INTO extras (price_per_day) VALUES (?);

-- 5. Помилка вводу: у полі ключа введено 'абв'. Integer.parseInt кидає NumberFormatException
-- ще до звернення до бази, SQL не надсилається.

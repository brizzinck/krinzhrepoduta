-- Помилки бази: сценарій demo/05-errors.txt. Звідки: src/main/java/rental/BranchDao.java і CarDao.java,
-- методи insert і delete; помилки перехоплює Main.java, метод describe(JdbiException). Програма не падає.

-- 1. Помилка 1062 (UNIQUE): назва філії вже є.
-- BranchDao.insert. Значення: :name = 'Прокат «Київ-Центр»', :city = 'Київ', :address = 'вул. Нова, 1', :phone = '+380440000000'.
INSERT INTO branches (name, city, address, phone) VALUES (:name, :city, :address, :phone);

-- 2. Помилка 3819 (CHECK chk_cars_price): від'ємна ціна.
-- CarDao.insert. Значення: :modelId = 1, :branchId = 1, :vin = 'TESTVIN0000000001', :plateNumber = 'TEST0001XX',
-- :manufactureYear = 2024, :mileageKm = 0, :dailyPrice = -5, :status = 'available'.
INSERT INTO cars (model_id, branch_id, vin, plate_number, manufacture_year, mileage_km, daily_price, status)
VALUES (:modelId, :branchId, :vin, :plateNumber, :manufactureYear, :mileageKm, :dailyPrice, :status);

-- 3. Помилка 1452 (FOREIGN KEY): філії 999 немає.
-- CarDao.insert, той самий текст. Значення: :branchId = 999, :vin = 'TESTVIN0000000002', :plateNumber = 'TEST0002XX', :dailyPrice = 900.

-- 4. Помилка 1451 (FOREIGN KEY, ON DELETE RESTRICT): на філію 1 посилаються авто.
-- BranchDao.delete. Значення: :id = 1.
DELETE FROM branches WHERE branch_id = :id;

-- 5. Помилка вводу: у полі ключа введено 'абв'. Integer.parseInt кидає NumberFormatException
-- ще до звернення до бази, SQL не надсилається.

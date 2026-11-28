-- Додати авто: меню пункт 3, таблиця 3 (cars).
-- Звідки: src/main/java/rental/CarDao.java, метод insert(Car), анотація @SqlUpdate з @GetGeneratedKeys.
-- Значення з demo/02-create.txt (беруться з запису Car через @BindMethods):
-- :modelId = 1, :branchId = 1, :vin = 'WVWZZZ1KZDP123456', :plateNumber = 'AA0001TT', :manufactureYear = 2024,
-- :mileageKm = 0 (порожній ввід, типове значення), :dailyPrice = 1500.00, :status = 'available' (типове значення).
-- Ключ car_id генерує MySQL, у демо це 25.
INSERT INTO cars (model_id, branch_id, vin, plate_number, manufacture_year, mileage_km, daily_price, status)
VALUES (:modelId, :branchId, :vin, :plateNumber, :manufactureYear, :mileageKm, :dailyPrice, :status);

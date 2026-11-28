-- Змінити авто: меню пункт 4, таблиця 3 (cars), ключ 25.
-- Звідки: src/main/java/rental/CarDao.java, методи findById(int) і update(Car), анотації @SqlQuery і @SqlUpdate.
-- Спершу Jdbi читає поточний запис (:id = 25), далі Main.update підставляє нові значення з demo/03-update.txt
-- (порожній ввід залишає поточне): :plateNumber без змін, :mileageKm = 12500, :dailyPrice = 1350.50,
-- :status = 'maintenance'; решта полів лишається з прочитаного запису; :carId = 25.
SELECT * FROM cars WHERE car_id = :id;

UPDATE cars SET model_id = :modelId, branch_id = :branchId, vin = :vin,
  plate_number = :plateNumber, manufacture_year = :manufactureYear, mileage_km = :mileageKm,
  daily_price = :dailyPrice, status = :status WHERE car_id = :carId;

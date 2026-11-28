-- Видалити авто: меню пункт 5, таблиця 3 (cars), ключ 25.
-- Звідки: src/main/java/rental/CarDao.java, метод delete(int), анотація @SqlUpdate.
-- Значення з demo/04-delete.txt: :id = 25.
DELETE FROM cars WHERE car_id = :id;

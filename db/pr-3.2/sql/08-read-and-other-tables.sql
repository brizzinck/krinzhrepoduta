-- Показати таблицю і знайти за ключем: меню пункти 1 і 2 (ручний показ, у make show не входить).
-- Звідки: BranchDao.java, CustomerDao.java, CarDao.java, методи findAll() і findById(int), анотація @SqlQuery.
-- Сценарії: demo/01-read.txt, demo/07-crud-all.txt (повний прохід CRUD по трьох таблицях).
SELECT * FROM branches ORDER BY branch_id;
SELECT * FROM branches WHERE branch_id = :id;
SELECT * FROM customers ORDER BY customer_id;
SELECT * FROM customers WHERE customer_id = :id;
SELECT * FROM cars ORDER BY car_id;
SELECT * FROM cars WHERE car_id = :id;

-- Додати, змінити, видалити для branches і customers (меню пункти 3, 4, 5, таблиці 1 і 2).
INSERT INTO branches (name, city, address, phone) VALUES (:name, :city, :address, :phone);
UPDATE branches SET name = :name, city = :city, address = :address, phone = :phone WHERE branch_id = :branchId;
DELETE FROM branches WHERE branch_id = :id;

INSERT INTO customers (first_name, last_name, middle_name, phone, email, birth_date,
  license_number, license_expires)
VALUES (:firstName, :lastName, :middleName, :phone, :email, :birthDate, :licenseNumber, :licenseExpires);
UPDATE customers SET first_name = :firstName, last_name = :lastName, middle_name = :middleName,
  phone = :phone, email = :email, birth_date = :birthDate, license_number = :licenseNumber,
  license_expires = :licenseExpires WHERE customer_id = :customerId;
DELETE FROM customers WHERE customer_id = :id;

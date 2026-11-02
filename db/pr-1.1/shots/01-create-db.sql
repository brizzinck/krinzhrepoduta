DROP DATABASE IF EXISTS car_rental;
CREATE DATABASE car_rental
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;
USE car_rental;

SHOW DATABASES;
SELECT @@character_set_database AS charset, @@collation_database AS collation;

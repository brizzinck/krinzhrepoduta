-- Схема навчальної БД сервісу прокату автомобілів (MySQL 8).
-- Виконується контейнером mysql автоматично при першому запуску.
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS users (
    id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    login         VARCHAR(32)  NOT NULL,
    first_name    VARCHAR(50)  NOT NULL,
    email         VARCHAR(254) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    phone         VARCHAR(16)  NOT NULL,
    birth_date    DATE         NOT NULL,
    role          VARCHAR(10)  NOT NULL DEFAULT 'CLIENT',
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_login UNIQUE (login),
    CONSTRAINT uq_users_email UNIQUE (email)
) DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS cars (
    id          BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    brand       VARCHAR(40)   NOT NULL,
    model       VARCHAR(40)   NOT NULL,
    production_year SMALLINT  NOT NULL,
    daily_price DECIMAL(8, 2) NOT NULL
) DEFAULT CHARSET = utf8mb4;

INSERT INTO cars (brand, model, production_year, daily_price) VALUES
    ('Toyota',  'Corolla', 2022, 1200.00),
    ('Toyota',  'Camry',   2023, 1800.00),
    ('Skoda',   'Octavia', 2021, 1100.00),
    ('Renault', 'Duster',  2022, 1300.00),
    ('BMW',     'X5',      2023, 3500.00);

-- Заздалегідь створений клієнт: його хеш має «витекти» у вразливому демо (UNION-ін'єкція).
-- Пароль у відкритому вигляді в БД не зберігається, лише BCrypt-хеш (cost 10).
INSERT INTO users (login, first_name, email, password_hash, phone, birth_date, role) VALUES
    ('manager', 'Ірина', 'manager@rental.example',
     '$2a$10$OMIQiy/8WhB.wovMRigmk.9.9ohWa7lmSvjiVjdyDI.m1vhycZciK', '+380501112233', '1990-05-17', 'ADMIN');

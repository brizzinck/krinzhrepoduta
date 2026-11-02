CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    login VARCHAR(32) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    phone VARCHAR(16) NOT NULL,
    birth_date DATE NOT NULL,
    role VARCHAR(10) NOT NULL DEFAULT 'CLIENT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_login UNIQUE (login),
    CONSTRAINT uq_users_email UNIQUE (email)
);
CREATE TABLE cars (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    brand VARCHAR(40) NOT NULL,
    model VARCHAR(40) NOT NULL,
    production_year SMALLINT NOT NULL,
    daily_price DECIMAL(8, 2) NOT NULL
);
INSERT INTO cars (brand, model, production_year, daily_price) VALUES
    ('Toyota', 'Corolla', 2022, 1200.00), ('Toyota', 'Camry', 2023, 1800.00), ('BMW', 'X5', 2023, 3500.00);
INSERT INTO users (login, first_name, email, password_hash, phone, birth_date, role)
    VALUES ('manager', 'Ірина', 'manager@rental.example', '$2a$10$hashforlabdemoonly', '+380501112233', '1990-05-17', 'ADMIN');

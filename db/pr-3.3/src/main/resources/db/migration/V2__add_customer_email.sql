-- Зміна моделі: клієнт отримує адресу електронної пошти
ALTER TABLE customers ADD COLUMN Email VARCHAR(100) NULL;

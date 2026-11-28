-- Міграція Flyway V2: клієнт отримує адресу електронної пошти.
-- Файл: src/main/resources/db/migration/V2__add_customer_email.sql (копія нижче).
-- Виконується командою  ./flyway.sh migrate  у make migrate.
-- До V2 застосунок не стартує: Hibernate з hbm2ddl=validate пише "Schema-validation: missing column [Email]",
-- бо сутність Customers має поле email, а в базі версії 1 колонки немає (make validate).
-- Зміна моделі: клієнт отримує адресу електронної пошти
ALTER TABLE customers ADD COLUMN Email VARCHAR(100) NULL;

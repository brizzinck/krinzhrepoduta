package northwind;

import northwind.entity.Categories;
import northwind.entity.Customerdemographics;
import northwind.entity.Customers;
import northwind.entity.Employees;
import northwind.entity.OrderDetails;
import northwind.entity.Orders;
import northwind.entity.Products;
import northwind.entity.Region;
import northwind.entity.Shippers;
import northwind.entity.Suppliers;
import northwind.entity.Territories;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/** Налаштування Hibernate. Параметри підключення беруться зі змінних середовища, у коді паролів немає. */
public final class Database {
    private Database() {
    }

    /** SessionFactory створюється один раз; вона перевіряє, що схема БД збігається з сутностями (validate). */
    public static SessionFactory open() {
        Configuration cfg = new Configuration()
            .setProperty("hibernate.connection.url", env("DB_URL"))
            .setProperty("hibernate.connection.username", env("DB_USER"))
            .setProperty("hibernate.connection.password", env("DB_PASSWORD"))
            // таблиця `order details` має пробіл в імені, тому всі імена беруться в лапки
            .setProperty("hibernate.globally_quoted_identifiers", "true")
            // міграціями схеми керує Flyway, Hibernate лише перевіряє її
            .setProperty("hibernate.hbm2ddl.auto", "validate")
            .setProperty("hibernate.generate_statistics", "true");
        if ("true".equals(System.getenv("SHOW_SQL"))) {
            cfg.setProperty("hibernate.show_sql", "true").setProperty("hibernate.format_sql", "true");
        }
        for (Class<?> entity : new Class<?>[] {Categories.class, Suppliers.class, Products.class, Customers.class,
            Employees.class, Shippers.class, Orders.class, OrderDetails.class, Region.class, Territories.class,
            Customerdemographics.class}) {
            cfg.addAnnotatedClass(entity);
        }
        return cfg.buildSessionFactory();
    }

    private static String env(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Не задано змінну середовища " + name);
        }
        return value;
    }
}

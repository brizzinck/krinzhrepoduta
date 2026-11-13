package northwind;

import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

/** Запити 3-6 з ПР 1.2 (варіант 2) мовою JPQL. Працюють із сутностями, а не з таблицями. */
public class Queries {
    private final SessionFactory factory;

    public Queries(SessionFactory factory) {
        this.factory = factory;
    }

    /** Версія сервера для заголовка сеансу. */
    public String serverVersion() {
        try (Session session = factory.openSession()) {
            return session.createNativeQuery("select version()", String.class).getSingleResult();
        }
    }

    /** Перші рядки таблиці: from Entity. */
    public void list(int table) {
        String[] entity = {"Categories", "Products", "Customers", "Employees"};
        String[][] headers = {
            {"categoryId", "categoryName", "description"},
            {"productId", "productName", "unitPrice", "unitsInStock"},
            {"customerId", "companyName", "city", "country"},
            {"employeeId", "firstName", "lastName", "city"}};
        String[] fields = {
            "c.categoryId, c.categoryName, c.description",
            "c.productId, c.productName, c.unitPrice, c.unitsInStock",
            "c.customerId, c.companyName, c.city, c.country",
            "c.employeeId, c.firstName, c.lastName, c.city"};
        try (Session session = factory.openSession()) {
            List<Object[]> rows = session
                .createQuery("select " + fields[table] + " from " + entity[table] + " c order by 1", Object[].class)
                .setMaxResults(10).getResultList();
            Printer.print(headers[table], rows, 0);
        }
    }

    /** Запит 3: середня ціна продуктів для кожної категорії. */
    public void avgPriceByCategory() {
        run(new String[] {"categoryName", "avgPrice"}, """
            select c.categoryName, round(avg(p.unitPrice), 2)
            from Products p join p.categories c
            group by c.categoryId, c.categoryName
            order by c.categoryName""", 0);
    }

    /** Запит 4: співробітники й клієнти по містах, де є співробітники (з'єднання сутностей за містом). */
    public void employeesAndCustomersByCity() {
        run(new String[] {"city", "employees", "customers"}, """
            select e.city, count(distinct e.employeeId), count(distinct c.customerId)
            from Employees e left join Customers c on c.city = e.city
            group by e.city
            order by e.city""", 0);
    }

    /** Запит 5: доходи від кожного клієнта з урахуванням знижки. */
    public void revenueByCustomer() {
        run(new String[] {"customerId", "companyName", "revenue"}, """
            select c.customerId, c.companyName, round(sum(od.unitPrice * od.quantity * (1 - od.discount)), 2) as revenue
            from OrderDetails od join od.orders o join o.customers c
            group by c.customerId, c.companyName
            order by revenue desc""", 10);
    }

    /** Запит 6: замовлення й співробітник через вкладений запит у select. */
    public void ordersWithEmployee() {
        run(new String[] {"orderId", "orderDate", "customerId", "employee"}, """
            select o.orderId, o.orderDate, o.customers.customerId,
                   (select concat(e.firstName, ' ', e.lastName) from Employees e where e.employeeId = o.employees.employeeId)
            from Orders o
            order by o.orderId""", 10);
    }

    private void run(String[] header, String jpql, int limit) {
        try (Session session = factory.openSession()) {
            Printer.print(header, session.createQuery(jpql, Object[].class).getResultList(), limit);
        }
    }

    /** Демонстрація N+1: ліниве завантаження дає 1 + N запитів, join fetch один. */
    public void nPlusOne() {
        Statistics stats = factory.getStatistics();
        try (Session session = factory.openSession()) {
            stats.clear();
            List<northwind.entity.Orders> orders = session
                .createQuery("from Orders o order by o.orderId", northwind.entity.Orders.class)
                .setMaxResults(10).getResultList();
            for (northwind.entity.Orders o : orders) {
                o.getEmployees().getLastName();
            }
            System.out.println("Ліниве завантаження: SQL-запитів " + stats.getPrepareStatementCount());
        }
        try (Session session = factory.openSession()) {
            stats.clear();
            List<northwind.entity.Orders> orders = session
                .createQuery("from Orders o join fetch o.employees order by o.orderId", northwind.entity.Orders.class)
                .setMaxResults(10).getResultList();
            for (northwind.entity.Orders o : orders) {
                o.getEmployees().getLastName();
            }
            System.out.println("join fetch: SQL-запитів " + stats.getPrepareStatementCount());
        }
    }
}

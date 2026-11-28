package rental;

import java.math.BigDecimal;
import java.util.List;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

/** Три запити з ПР 3.1: з об'єднанням таблиць, із фільтрацією, з агрегатними функціями. */
public interface ReportDao {
    @RegisterConstructorMapper(ActiveRental.class)
    @SqlQuery("""
        SELECT r.rental_id, CONCAT(c.last_name, ' ', c.first_name) AS customer,
               CONCAT(m.brand, ' ', m.model_name) AS car, a.plate_number,
               b.city AS pickup_city, r.planned_end_at
        FROM rentals r
        JOIN customers c ON c.customer_id = r.customer_id
        JOIN cars a ON a.car_id = r.car_id
        JOIN car_models m ON m.model_id = a.model_id
        JOIN branches b ON b.branch_id = r.pickup_branch_id
        WHERE r.status = 'active'
        ORDER BY r.planned_end_at""")
    List<ActiveRental> activeRentals();

    @RegisterConstructorMapper(FreeCar.class)
    @SqlQuery("""
        SELECT a.car_id, CONCAT(m.brand, ' ', m.model_name) AS car, a.plate_number, b.city, a.daily_price
        FROM cars a
        JOIN car_models m ON m.model_id = a.model_id
        JOIN branches b ON b.branch_id = a.branch_id
        WHERE a.status = 'available' AND b.city = :city AND a.daily_price <= :maxPrice
        ORDER BY a.daily_price""")
    List<FreeCar> freeCars(@Bind("city") String city, @Bind("maxPrice") BigDecimal maxPrice);

    @RegisterConstructorMapper(BranchRevenue.class)
    @SqlQuery("""
        SELECT b.name AS branch, COUNT(DISTINCT r.rental_id) AS rentals,
               COUNT(p.payment_id) AS payments, SUM(p.amount) AS total,
               ROUND(AVG(p.amount), 2) AS avg_payment
        FROM branches b
        JOIN rentals r ON r.pickup_branch_id = b.branch_id
        JOIN payments p ON p.rental_id = r.rental_id
        WHERE p.kind IN ('rent', 'fine')
        GROUP BY b.branch_id, b.name
        ORDER BY total DESC""")
    List<BranchRevenue> branchRevenue();
}

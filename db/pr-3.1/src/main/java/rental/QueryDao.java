package rental;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Три запити предметної області: з об'єднанням таблиць, із фільтрацією, з агрегатними функціями. */
public class QueryDao {
    private static final String ACTIVE_RENTALS = """
        SELECT r.rental_id, CONCAT(c.last_name, ' ', c.first_name) AS customer,
               CONCAT(m.brand, ' ', m.model_name) AS car, a.plate_number,
               b.city AS pickup_city, r.planned_end_at
        FROM rentals r
        JOIN customers c ON c.customer_id = r.customer_id
        JOIN cars a ON a.car_id = r.car_id
        JOIN car_models m ON m.model_id = a.model_id
        JOIN branches b ON b.branch_id = r.pickup_branch_id
        WHERE r.status = 'active'
        ORDER BY r.planned_end_at""";

    private static final String FREE_CARS = """
        SELECT a.car_id, CONCAT(m.brand, ' ', m.model_name) AS car, a.plate_number, b.city, a.daily_price
        FROM cars a
        JOIN car_models m ON m.model_id = a.model_id
        JOIN branches b ON b.branch_id = a.branch_id
        WHERE a.status = 'available' AND b.city = ? AND a.daily_price <= ?
        ORDER BY a.daily_price""";

    private static final String BRANCH_REVENUE = """
        SELECT b.name AS branch, COUNT(DISTINCT r.rental_id) AS rentals, COUNT(p.payment_id) AS payments,
               SUM(p.amount) AS total, ROUND(AVG(p.amount), 2) AS avg_payment
        FROM branches b
        JOIN rentals r ON r.pickup_branch_id = b.branch_id
        JOIN payments p ON p.rental_id = r.rental_id
        WHERE p.kind IN ('rent', 'fine')
        GROUP BY b.branch_id, b.name
        ORDER BY total DESC""";

    private final Connection connection;

    public QueryDao(Connection connection) {
        this.connection = connection;
    }

    public int activeRentals() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(ACTIVE_RENTALS); ResultSet rs = ps.executeQuery()) {
            return Printer.print(rs);
        }
    }

    public int freeCars(String city, BigDecimal maxPrice) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(FREE_CARS)) {
            ps.setString(1, city);
            ps.setBigDecimal(2, maxPrice);
            try (ResultSet rs = ps.executeQuery()) {
                return Printer.print(rs);
            }
        }
    }

    public int branchRevenue() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(BRANCH_REVENUE); ResultSet rs = ps.executeQuery()) {
            return Printer.print(rs);
        }
    }
}

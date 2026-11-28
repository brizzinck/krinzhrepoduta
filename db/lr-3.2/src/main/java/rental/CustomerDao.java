package rental;

import java.util.List;
import java.util.Optional;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

/** CRUD для customers. */
@RegisterConstructorMapper(Customer.class)
public interface CustomerDao {
    @SqlQuery("SELECT * FROM customers ORDER BY customer_id")
    List<Customer> findAll();

    @SqlQuery("SELECT * FROM customers WHERE customer_id = :id")
    Optional<Customer> findById(@Bind("id") int id);

    @SqlUpdate("INSERT INTO customers (first_name, last_name, middle_name, phone, email, birth_date, "
        + "license_number, license_expires) "
        + "VALUES (:firstName, :lastName, :middleName, :phone, :email, :birthDate, :licenseNumber, :licenseExpires)")
    @GetGeneratedKeys
    int insert(@BindMethods Customer customer);

    @SqlUpdate("UPDATE customers SET first_name = :firstName, last_name = :lastName, middle_name = :middleName, "
        + "phone = :phone, email = :email, birth_date = :birthDate, license_number = :licenseNumber, "
        + "license_expires = :licenseExpires WHERE customer_id = :customerId")
    int update(@BindMethods Customer customer);

    @SqlUpdate("DELETE FROM customers WHERE customer_id = :id")
    int delete(@Bind("id") int id);
}

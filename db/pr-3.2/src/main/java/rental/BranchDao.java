package rental;

import java.util.List;
import java.util.Optional;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

/** CRUD для branches: інтерфейс із SQL в анотаціях, реалізацію створює Jdbi. */
@RegisterConstructorMapper(Branch.class)
public interface BranchDao {
    @SqlQuery("SELECT * FROM branches ORDER BY branch_id")
    List<Branch> findAll();

    @SqlQuery("SELECT * FROM branches WHERE branch_id = :id")
    Optional<Branch> findById(@Bind("id") int id);

    @SqlUpdate("INSERT INTO branches (name, city, address, phone) VALUES (:name, :city, :address, :phone)")
    @GetGeneratedKeys
    int insert(@BindMethods Branch branch);

    @SqlUpdate("UPDATE branches SET name = :name, city = :city, address = :address, phone = :phone "
        + "WHERE branch_id = :branchId")
    int update(@BindMethods Branch branch);

    @SqlUpdate("DELETE FROM branches WHERE branch_id = :id")
    int delete(@Bind("id") int id);
}

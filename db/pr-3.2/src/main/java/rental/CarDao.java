package rental;

import java.util.List;
import java.util.Optional;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

/** CRUD для cars. */
@RegisterConstructorMapper(Car.class)
public interface CarDao {
    @SqlQuery("SELECT * FROM cars ORDER BY car_id")
    List<Car> findAll();

    @SqlQuery("SELECT * FROM cars WHERE car_id = :id")
    Optional<Car> findById(@Bind("id") int id);

    // @BindMethods бере значення з методів запису: :modelId це car.modelId()
    @SqlUpdate("INSERT INTO cars (model_id, branch_id, vin, plate_number, manufacture_year, mileage_km, "
        + "daily_price, status) "
        + "VALUES (:modelId, :branchId, :vin, :plateNumber, :manufactureYear, :mileageKm, :dailyPrice, :status)")
    @GetGeneratedKeys
    int insert(@BindMethods Car car);

    @SqlUpdate("UPDATE cars SET model_id = :modelId, branch_id = :branchId, vin = :vin, "
        + "plate_number = :plateNumber, manufacture_year = :manufactureYear, mileage_km = :mileageKm, "
        + "daily_price = :dailyPrice, status = :status WHERE car_id = :carId")
    int update(@BindMethods Car car);

    @SqlUpdate("DELETE FROM cars WHERE car_id = :id")
    int delete(@Bind("id") int id);
}

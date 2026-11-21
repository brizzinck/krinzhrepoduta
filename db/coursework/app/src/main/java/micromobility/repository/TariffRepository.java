package micromobility.repository;

import java.util.List;
import java.util.Optional;
import micromobility.domain.Tariff;
import micromobility.domain.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TariffRepository extends JpaRepository<Tariff, Integer> {

    /** BR7, BR9: the tariff of an operator for a vehicle type. */
    Optional<Tariff> findByOperatorIdAndVehicleType(Integer operatorId, VehicleType vehicleType);

    boolean existsByOperatorId(Integer operatorId);

    long countByOperatorId(Integer operatorId);

    /** List page: operator loaded in the same query (the GORM Preload analogue); empty filter = no filter. */
    @Query("""
            select t from Tariff t join fetch t.operator o
            where (:opId is null or o.id = :opId) and (:type is null or t.vehicleType = :type)
            order by o.name, t.vehicleType, t.id""")
    List<Tariff> search(@Param("opId") Integer opId, @Param("type") VehicleType type);
}

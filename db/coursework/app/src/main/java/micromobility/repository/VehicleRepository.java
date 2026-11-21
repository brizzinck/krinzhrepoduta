package micromobility.repository;

import java.util.List;
import micromobility.domain.Operator;
import micromobility.domain.Station;
import micromobility.domain.Vehicle;
import micromobility.domain.VehicleStatus;
import micromobility.domain.VehicleType;
import micromobility.report.AvailableVehicleRow;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {

    long countByStationId(Integer stationId);

    boolean existsByOperatorId(Integer operatorId);

    long countByOperatorId(Integer operatorId);

    long countByOperatorIdAndVehicleType(Integer operatorId, VehicleType vehicleType);

    boolean existsByOperatorIdAndVehicleType(Integer operatorId, VehicleType vehicleType);

    boolean existsByStationId(Integer stationId);

    /** List page with filters; an empty filter means no filter. Parents are fetched in the same query. */
    @Query("""
            select v from Vehicle v join fetch v.operator o left join fetch v.station s
            where (:opId is null or o.id = :opId) and (:type is null or v.vehicleType = :type)
              and (:status is null or v.status = :status) and (:stationId is null or s.id = :stationId)
              and v.batteryPct between :minBattery and :maxBattery
            order by v.code""")
    List<Vehicle> search(@Param("opId") Integer opId, @Param("type") VehicleType type,
                         @Param("status") VehicleStatus status, @Param("stationId") Integer stationId,
                         @Param("minBattery") int minBattery, @Param("maxBattery") int maxBattery);

    /** R7: vehicles to charge. EntityGraph loads operator and station without N+1. */
    @EntityGraph(attributePaths = {"operator", "station"})
    List<Vehicle> findByStatusAndBatteryPctLessThanOrderByBatteryPctAscCodeAsc(VehicleStatus status, int level);

    /** R1: vehicles that can be unlocked now, cheapest 10-minute ride first. */
    @Query("""
            select new micromobility.report.AvailableVehicleRow(v.id, v.code, s.name, o.name, v.vehicleType, v.model,
                       v.batteryPct, tr.unlockFeeKop, tr.perMinuteKop)
            from Vehicle v join v.operator o join v.station s
                 join Tariff tr on tr.operator = o and tr.vehicleType = v.vehicleType
            where v.status = :available and v.batteryPct >= :minBattery
              and (:opId is null or o.id = :opId) and (:type is null or v.vehicleType = :type)
              and (:stationId is null or s.id = :stationId)
              and not exists (select 1 from Ride r where r.vehicle = v and r.endedAt is null)
            order by tr.unlockFeeKop + 10 * tr.perMinuteKop, v.batteryPct desc, v.code""")
    List<AvailableVehicleRow> findAvailable(@Param("available") VehicleStatus available,
                                            @Param("minBattery") int minBattery, @Param("opId") Integer opId,
                                            @Param("type") VehicleType type, @Param("stationId") Integer stationId);

    /**
     * Takes the vehicle for a ride: one conditional UPDATE sets status 2 and clears the station (BR21, BR22).
     * Returns 1 only if the vehicle was still AVAILABLE with enough battery (GORM: Where(...).Updates + RowsAffected).
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Vehicle v set v.status = :inRide, v.station = null
            where v.id = :id and v.status = :available and v.batteryPct >= :minBattery""")
    int markInRide(@Param("id") Integer id, @Param("available") VehicleStatus available,
                   @Param("inRide") VehicleStatus inRide, @Param("minBattery") int minBattery);

    /** Returns the vehicle from a ride: status 1 and the chosen station, only if it was IN_RIDE (finish, cancel). */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Vehicle v set v.status = :available, v.station = :station
            where v.id = :id and v.status = :inRide""")
    int markAvailable(@Param("id") Integer id, @Param("station") Station station,
                      @Param("available") VehicleStatus available, @Param("inRide") VehicleStatus inRide);

    /** Vehicle form save: never overwrites a vehicle that a ride took while the form was open (expect 1 row). */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Vehicle v set v.code = :code, v.operator = :operator, v.vehicleType = :type, v.model = :model,
                   v.batteryPct = :battery, v.status = :status, v.station = :station
            where v.id = :id and v.status <> :inRide""")
    int updateFromForm(@Param("id") Integer id, @Param("code") String code, @Param("operator") Operator operator,
                       @Param("type") VehicleType type, @Param("model") String model,
                       @Param("battery") Integer battery, @Param("status") VehicleStatus status,
                       @Param("station") Station station, @Param("inRide") VehicleStatus inRide);
}

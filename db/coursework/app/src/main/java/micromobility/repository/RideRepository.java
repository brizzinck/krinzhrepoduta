package micromobility.repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import micromobility.domain.PaymentKind;
import micromobility.domain.Ride;
import micromobility.domain.VehicleType;
import micromobility.report.RevenueRow;
import micromobility.report.RideRow;
import micromobility.report.PivotRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RideRepository extends JpaRepository<Ride, Integer> {

    boolean existsByCustomerIdAndEndedAtIsNull(Integer customerId);

    boolean existsByVehicleIdAndEndedAtIsNull(Integer vehicleId);

    boolean existsByVehicleId(Integer vehicleId);

    boolean existsByCustomerId(Integer customerId);

    long countByVehicleId(Integer vehicleId);

    long countByCustomerId(Integer customerId);

    /** SELECT ... FOR UPDATE on the ride: finish, cancel and refund of one ride are serialized. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Ride> findForUpdateById(Integer id);

    /** The ride with its customer, vehicle and operator for the receipt page. */
    @Query("""
            select r from Ride r join fetch r.customer join fetch r.vehicle v join fetch v.operator
            where r.id = :id""")
    Optional<Ride> findForReceipt(@Param("id") Integer id);

    /** R2: ride search; every filter is optional. */
    @Query("""
            select new micromobility.report.RideRow(r.id, c.lastName, c.firstName, v.code, o.name, v.vehicleType,
                       r.startedAt, r.endedAt, ch.amountKop, rf.amountKop, r.commissionKop)
            from Ride r join r.customer c join r.vehicle v join v.operator o
                 left join Payment ch on ch.ride = r and ch.kind = :charge
                 left join Payment rf on rf.ride = r and rf.kind = :refund
            where (:periodFrom is null or r.startedAt >= :periodFrom) and (:periodTo is null or r.startedAt < :periodTo)
              and (:opId is null or o.id = :opId) and (:type is null or v.vehicleType = :type)
              and (:prefix is null or c.lastName like :prefix escape '!' or c.phone like :suffix escape '!')
              and (:active is null or (:active = true and r.endedAt is null)
                                   or (:active = false and r.endedAt is not null))
            order by r.startedAt desc, r.id desc""")
    List<RideRow> searchRides(@Param("charge") PaymentKind charge, @Param("refund") PaymentKind refund,
                              @Param("periodFrom") LocalDateTime periodFrom, @Param("periodTo") LocalDateTime periodTo,
                              @Param("opId") Integer opId, @Param("type") VehicleType type,
                              @Param("prefix") String prefix, @Param("suffix") String suffix,
                              @Param("active") Boolean active);

    /**
     * R3: charged, refunded and stored commission by month of the finish (Kyiv time) and operator.
     * The period bounds are UTC; the month is the month of the finish in Europe/Kyiv.
     * function('convert_tz', ...) calls the MySQL function from HQL; it is repeated in select, group by and order by
     * because HQL has no select alias in group by; it needs the time zone tables that the Docker image loads.
     */
    @Query("""
            select new micromobility.report.RevenueRow(year(function('convert_tz', r.endedAt, '+00:00', 'Europe/Kyiv')),
                       month(function('convert_tz', r.endedAt, '+00:00', 'Europe/Kyiv')), o.name, count(r),
                       -sum(ch.amountKop), coalesce(sum(rf.amountKop), 0), sum(r.commissionKop))
            from Ride r join Payment ch on ch.ride = r and ch.kind = :charge
                 left join Payment rf on rf.ride = r and rf.kind = :refund
                 join r.vehicle v join v.operator o
            where (:periodFrom is null or r.endedAt >= :periodFrom) and (:periodTo is null or r.endedAt < :periodTo)
            group by year(function('convert_tz', r.endedAt, '+00:00', 'Europe/Kyiv')),
                     month(function('convert_tz', r.endedAt, '+00:00', 'Europe/Kyiv')), o.id, o.name
            order by year(function('convert_tz', r.endedAt, '+00:00', 'Europe/Kyiv')),
                     month(function('convert_tz', r.endedAt, '+00:00', 'Europe/Kyiv')), sum(ch.amountKop), o.id""")
    List<RevenueRow> revenue(@Param("charge") PaymentKind charge, @Param("refund") PaymentKind refund,
                             @Param("periodFrom") LocalDateTime periodFrom, @Param("periodTo") LocalDateTime periodTo);

    /** R5: operators x vehicle types, WITH ROLLUP adds the total row (no JPQL form, so native SQL). */
    @Query(value = """
            SELECT o.name AS operator, GROUPING(o.name) AS isTotal,
                   SUM(v.vehicle_type = 1) AS scooterRides,
                   ROUND(AVG(CASE WHEN v.vehicle_type = 1
                             THEN GREATEST(1, CEIL(TIMESTAMPDIFF(SECOND, r.started_at, r.ended_at) / 60)) END), 1) AS scooterAvgMin,
                   SUM(v.vehicle_type = 2) AS bikeRides,
                   ROUND(AVG(CASE WHEN v.vehicle_type = 2
                             THEN GREATEST(1, CEIL(TIMESTAMPDIFF(SECOND, r.started_at, r.ended_at) / 60)) END), 1) AS bikeAvgMin,
                   COUNT(*) AS totalRides
            FROM rides r
            JOIN vehicles v  ON v.id = r.vehicle_id
            JOIN operators o ON o.id = v.operator_id
            WHERE r.ended_at IS NOT NULL
              AND (:periodFrom IS NULL OR r.started_at >= :periodFrom) AND (:periodTo IS NULL OR r.started_at < :periodTo)
            GROUP BY o.name WITH ROLLUP
            ORDER BY GROUPING(o.name), o.name""", nativeQuery = true)
    List<PivotRow> pivot(@Param("periodFrom") LocalDateTime periodFrom, @Param("periodTo") LocalDateTime periodTo);
}

package micromobility.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import micromobility.domain.Payment;
import micromobility.domain.PaymentKind;
import micromobility.report.TopCustomerRow;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    /** Wallet balance = sum of the customer's payments (kopecks). */
    @Query("select coalesce(sum(p.amountKop), 0) from Payment p where p.customer.id = :id")
    long balanceOf(@Param("id") Integer id);

    /** D28: a customer with payments other than the bonus row cannot be deleted. */
    boolean existsByCustomerIdAndKindNot(Integer customerId, PaymentKind kind);

    long countByCustomerIdAndKindNot(Integer customerId, PaymentKind kind);

    List<Payment> findByCustomerIdAndKind(Integer customerId, PaymentKind kind);

    /** The charge (RIDE) or the refund (REFUND) of a ride: at most one of each (uq_payments_ride_kind). */
    Optional<Payment> findByRideIdAndKind(Integer rideId, PaymentKind kind);

    /** Wallet history page: newest first; customerId null = all customers. */
    @Query("""
            select p from Payment p join fetch p.customer c left join fetch p.ride
            where (:customerId is null or c.id = :customerId)
            order by p.createdAt desc, p.id desc""")
    List<Payment> history(@Param("customerId") Integer customerId, Pageable limit);

    /** R4: top customers by net spend (charges minus refunds) of rides finished in a period, with at least N rides. */
    @Query("""
            select new micromobility.report.TopCustomerRow(c.id, c.lastName, c.firstName,
                       count(distinct r.id), -sum(p.amountKop))
            from Payment p join p.ride r join r.customer c
            where p.kind in :kinds
              and (:periodFrom is null or r.endedAt >= :periodFrom) and (:periodTo is null or r.endedAt < :periodTo)
            group by c.id, c.lastName, c.firstName
            having count(distinct r.id) >= :minRides
            order by sum(p.amountKop) asc, c.id""")
    List<TopCustomerRow> topCustomers(@Param("kinds") List<PaymentKind> kinds,
                                      @Param("periodFrom") LocalDateTime periodFrom,
                                      @Param("periodTo") LocalDateTime periodTo,
                                      @Param("minRides") long minRides, Pageable limit);
}

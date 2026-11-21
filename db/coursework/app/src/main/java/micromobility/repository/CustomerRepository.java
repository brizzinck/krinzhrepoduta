package micromobility.repository;

import java.util.List;
import micromobility.domain.Customer;
import micromobility.report.BalanceRow;
import micromobility.report.CustomerRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    /** Customers who passed the Diia check (the only ones who can start a ride). */
    List<Customer> findByDiiaVerifiedAtIsNotNullOrderByLastNameAscIdAsc();

    /** Customers list with the derived balance; prefix null = all. */
    @Query("""
            select new micromobility.report.CustomerRow(c.id, c.lastName, c.firstName, c.phone, c.email,
                       c.diiaVerifiedAt, coalesce(sum(p.amountKop), 0))
            from Customer c left join Payment p on p.customer = c
            where (:prefix is null or c.lastName like :prefix escape '!')
            group by c.id, c.lastName, c.firstName, c.phone, c.email, c.diiaVerifiedAt
            order by c.lastName, c.firstName, c.id""")
    List<CustomerRow> rows(@Param("prefix") String prefix);

    /** Sets the Diia time once: a stale edit form can never clear it (expect 1 row). */
    @org.springframework.data.jpa.repository.Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Customer c set c.diiaVerifiedAt = :at where c.id = :id and c.diiaVerifiedAt is null")
    int markVerified(@Param("id") Integer id, @Param("at") java.time.LocalDateTime at);

    /** R6: wallet balances below a threshold. */
    @Query("""
            select new micromobility.report.BalanceRow(c.id, c.lastName, c.firstName, c.phone,
                       coalesce(sum(p.amountKop), 0), coalesce(sum(case when p.kind = :topup then 1 else 0 end), 0))
            from Customer c left join Payment p on p.customer = c
            group by c.id, c.lastName, c.firstName, c.phone
            having coalesce(sum(p.amountKop), 0) < :threshold
            order by coalesce(sum(p.amountKop), 0), c.id""")
    List<BalanceRow> findBelowBalance(@Param("topup") micromobility.domain.PaymentKind topup,
                                      @Param("threshold") long threshold);
}

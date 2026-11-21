package micromobility.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.Immutable;

/**
 * Table payments: the wallet ledger, insert-only (@Immutable: Hibernate never issues an UPDATE).
 * The composite FK fk_payments_ride_customer (ride_id, customer_id) is a database guard and is not mapped;
 * a charge or refund takes its customer from ride.getCustomer().
 */
@Entity
@Immutable
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /** NULL for BONUS and TOPUP. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ride_id")
    private Ride ride;

    @Column(name = "kind", nullable = false, columnDefinition = "tinyint unsigned")
    private PaymentKind kind;

    /** Signed: money in is positive, a ride charge is negative. */
    @Column(name = "amount_kop", nullable = false)
    private int amountKop;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Payment() {
    }

    public Payment(Customer customer, Ride ride, PaymentKind kind, int amountKop, LocalDateTime createdAt) {
        this.customer = customer;
        this.ride = ride;
        this.kind = kind;
        this.amountKop = amountKop;
        this.createdAt = createdAt;
    }

    public Integer getId() { return id; }
    public Customer getCustomer() { return customer; }
    public Ride getRide() { return ride; }
    public PaymentKind getKind() { return kind; }
    public int getAmountKop() { return amountKop; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

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

/**
 * Table rides: one rental. ended_at IS NULL means the ride is active. Customer, vehicle, start time and the
 * two prices are written once (updatable = false); only ended_at and commission_kop change, at the finish.
 */
@Entity
@Table(name = "rides")
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false, updatable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false, updatable = false)
    private Vehicle vehicle;

    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    /** Copy of the tariff at the start (copy a). */
    @Column(name = "unlock_fee_kop", nullable = false, updatable = false)
    private int unlockFeeKop;

    @Column(name = "per_minute_kop", nullable = false, updatable = false)
    private int perMinuteKop;

    /** Fixed at the finish from the operator's percentage of that moment (copy b). */
    @Column(name = "commission_kop")
    private Integer commissionKop;

    public Integer getId() { return id; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }
    public int getUnlockFeeKop() { return unlockFeeKop; }
    public void setUnlockFeeKop(int unlockFeeKop) { this.unlockFeeKop = unlockFeeKop; }
    public int getPerMinuteKop() { return perMinuteKop; }
    public void setPerMinuteKop(int perMinuteKop) { this.perMinuteKop = perMinuteKop; }
    public Integer getCommissionKop() { return commissionKop; }
    public void setCommissionKop(Integer commissionKop) { this.commissionKop = commissionKop; }
    public boolean isActive() { return endedAt == null; }
}

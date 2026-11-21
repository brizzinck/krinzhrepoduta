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

/** Table tariffs: the price of one vehicle type at one operator (unique per operator and type). */
@Entity
@Table(name = "tariffs")
public class Tariff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operator_id", nullable = false)
    private Operator operator;

    @Column(name = "vehicle_type", nullable = false, columnDefinition = "tinyint unsigned")
    private VehicleType vehicleType;

    @Column(name = "unlock_fee_kop", nullable = false)
    private int unlockFeeKop;

    @Column(name = "per_minute_kop", nullable = false)
    private int perMinuteKop;

    public Integer getId() { return id; }
    public Operator getOperator() { return operator; }
    public void setOperator(Operator operator) { this.operator = operator; }
    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }
    public int getUnlockFeeKop() { return unlockFeeKop; }
    public void setUnlockFeeKop(int unlockFeeKop) { this.unlockFeeKop = unlockFeeKop; }
    public int getPerMinuteKop() { return perMinuteKop; }
    public void setPerMinuteKop(int perMinuteKop) { this.perMinuteKop = perMinuteKop; }
}

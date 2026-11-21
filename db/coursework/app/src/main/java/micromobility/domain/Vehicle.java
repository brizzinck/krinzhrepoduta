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

/**
 * Table vehicles: one physical scooter or bike. The composite FK fk_vehicles_tariff
 * (operator_id, vehicle_type) is a database guard and is not mapped as an association.
 */
@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "code", nullable = false, length = 8)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operator_id", nullable = false)
    private Operator operator;

    /** TINYINT UNSIGNED code through VehicleTypeConverter. */
    @Column(name = "vehicle_type", nullable = false, columnDefinition = "tinyint unsigned")
    private VehicleType vehicleType;

    @Column(name = "model", nullable = false, length = 40)
    private String model;

    /** TINYINT UNSIGNED, CHECK 0-100. */
    @Column(name = "battery_pct", nullable = false, columnDefinition = "tinyint unsigned")
    private Integer batteryPct;

    /** TINYINT UNSIGNED code through VehicleStatusConverter; the DB default 1 is repeated here (Hibernate sends every column). */
    @Column(name = "status", nullable = false, columnDefinition = "tinyint unsigned")
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    /** NULL unless the status is AVAILABLE (chk_vehicles_station). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id")
    private Station station;

    public Integer getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Operator getOperator() { return operator; }
    public void setOperator(Operator operator) { this.operator = operator; }
    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public Integer getBatteryPct() { return batteryPct; }
    public void setBatteryPct(Integer batteryPct) { this.batteryPct = batteryPct; }
    public VehicleStatus getStatus() { return status; }
    public void setStatus(VehicleStatus status) { this.status = status; }
    public Station getStation() { return station; }
    public void setStation(Station station) { this.station = station; }
}

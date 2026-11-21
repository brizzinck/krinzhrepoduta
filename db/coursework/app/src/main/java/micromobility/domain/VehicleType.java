package micromobility.domain;

/** vehicles.vehicle_type and tariffs.vehicle_type: TINYINT UNSIGNED code (CHECK 1-2). */
public enum VehicleType {
    E_SCOOTER(1, "Електросамокат"),
    E_BIKE(2, "Електровелосипед");

    private final int code;
    private final String label;

    VehicleType(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static VehicleType fromCode(int code) {
        for (VehicleType t : values()) {
            if (t.code == code) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown vehicle type code: " + code);
    }
}

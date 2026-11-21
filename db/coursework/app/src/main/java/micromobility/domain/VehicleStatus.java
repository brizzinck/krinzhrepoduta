package micromobility.domain;

/** vehicles.status: TINYINT UNSIGNED code (CHECK 1-5). IN_RIDE is written only by RideService (BR21). */
public enum VehicleStatus {
    AVAILABLE(1, "Чекає клієнта"),
    IN_RIDE(2, "У поїздці"),
    CHARGING(3, "На зарядці"),
    REPAIR(4, "У ремонті"),
    RETIRED(5, "Списано");

    private final int code;
    private final String label;

    VehicleStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static VehicleStatus fromCode(int code) {
        for (VehicleStatus s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown vehicle status code: " + code);
    }
}

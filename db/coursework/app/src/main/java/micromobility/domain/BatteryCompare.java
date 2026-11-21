package micromobility.domain;

/**
 * Comparison of the battery filter on the vehicles list. The enum turns "more than / less than / equal to b"
 * into one closed range [min, max], so the search is a single readable JPQL query:
 * {@code v.batteryPct between :minBattery and :maxBattery}. GORM analogue: {@code Where("battery_pct BETWEEN ? AND ?", min, max)}.
 */
public enum BatteryCompare {
    ANY("будь-який"),
    GT("більше ніж"),
    LT("менше ніж"),
    EQ("дорівнює");

    private final String label;

    BatteryCompare(String label) {
        this.label = label;
    }

    public String getLabel() { return label; }

    /** Lowest battery value that passes (0 when there is no filter). */
    public int min(Integer b) {
        if (this == ANY || b == null) {
            return 0;
        }
        return this == GT ? b + 1 : this == EQ ? b : 0;
    }

    /** Highest battery value that passes (100 when there is no filter); an empty range when b is 0 and LT. */
    public int max(Integer b) {
        if (this == ANY || b == null) {
            return 100;
        }
        return this == LT ? b - 1 : this == EQ ? b : 100;
    }
}

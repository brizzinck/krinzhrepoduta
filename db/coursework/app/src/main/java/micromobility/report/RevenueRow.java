package micromobility.report;

/** R3 row: one month of the finish and one operator. */
public record RevenueRow(Integer year, Integer month, String operator, Long rides,
                         Long chargedKop, Long refundedKop, Long commissionKop) {

    public String period() {
        return String.format("%04d-%02d", year, month);
    }

    public long netKop() {
        return chargedKop - refundedKop;
    }
}

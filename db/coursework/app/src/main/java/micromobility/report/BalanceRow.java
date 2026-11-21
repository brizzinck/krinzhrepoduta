package micromobility.report;

/** R6 row. */
public record BalanceRow(Integer id, String lastName, String firstName, String phone, Long balanceKop, Long topups) {

    public String customer() {
        return lastName + " " + firstName;
    }
}

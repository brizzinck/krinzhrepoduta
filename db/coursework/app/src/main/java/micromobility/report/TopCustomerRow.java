package micromobility.report;

/** R4 row. */
public record TopCustomerRow(Integer id, String lastName, String firstName, Long rides, Long spentKop) {

    public String customer() {
        return lastName + " " + firstName;
    }

    public long avgRideKop() {
        return Math.round((double) spentKop / rides);
    }
}

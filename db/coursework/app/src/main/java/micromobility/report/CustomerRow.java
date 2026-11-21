package micromobility.report;

import java.time.LocalDateTime;

/** Row of the customers list: the balance is derived (sum of payments), never stored. */
public record CustomerRow(Integer id, String lastName, String firstName, String phone, String email,
                          LocalDateTime diiaVerifiedAt, Long balanceKop) {

    public String fullName() {
        return lastName + " " + firstName;
    }

    public boolean verified() {
        return diiaVerifiedAt != null;
    }
}

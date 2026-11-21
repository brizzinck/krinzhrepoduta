package micromobility.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/** Table customers: a person who rents; the wallet is the sum of the customer's payments. */
@Entity
@org.hibernate.annotations.DynamicUpdate
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "last_name", nullable = false, length = 40)
    private String lastName;

    @Column(name = "first_name", nullable = false, length = 40)
    private String firstName;

    @Column(name = "phone", nullable = false, length = 13)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    /** Written once by CustomerService from the Clock; never updated. */
    @Column(name = "registered_at", nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    /** Set only by CustomerService.verifyWithDiia; NULL means not verified. */
    @Column(name = "diia_verified_at")
    private LocalDateTime diiaVerifiedAt;

    public Integer getId() { return id; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
    public LocalDateTime getDiiaVerifiedAt() { return diiaVerifiedAt; }
    public void setDiiaVerifiedAt(LocalDateTime diiaVerifiedAt) { this.diiaVerifiedAt = diiaVerifiedAt; }
    public boolean isVerified() { return diiaVerifiedAt != null; }
    public String getFullName() { return lastName + " " + firstName; }
}

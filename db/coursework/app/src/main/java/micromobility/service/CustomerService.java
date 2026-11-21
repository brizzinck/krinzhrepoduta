package micromobility.service;

import java.time.Clock;
import java.time.LocalDateTime;
import micromobility.config.ClockConfig;
import micromobility.domain.Customer;
import micromobility.domain.Payment;
import micromobility.domain.PaymentKind;
import micromobility.repository.CustomerRepository;
import micromobility.repository.PaymentRepository;
import micromobility.repository.RideRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    /** Start bonus of a new customer, 200 UAH (BR12). */
    public static final int BONUS_KOP = 20000;

    private final CustomerRepository customers;
    private final PaymentRepository payments;
    private final RideRepository rides;
    private final Clock clock;

    public CustomerService(CustomerRepository customers, PaymentRepository payments, RideRepository rides, Clock clock) {
        this.customers = customers;
        this.payments = payments;
        this.rides = rides;
        this.clock = clock;
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    /** BR12: the customer and the 200 UAH bonus row are written in one transaction. */
    @Transactional
    public Customer create(String lastName, String firstName, String phone, String email) {
        Customer c = new Customer();
        c.setLastName(lastName.trim());
        c.setFirstName(firstName.trim());
        c.setPhone(phone.trim());
        c.setEmail(blankToNull(email));
        c.setRegisteredAt(ClockConfig.now(clock));
        customers.saveAndFlush(c);
        payments.saveAndFlush(new Payment(c, null, PaymentKind.BONUS, BONUS_KOP, c.getRegisteredAt()));
        return c;
    }

    /** The edit form changes names, phone and e-mail; Diia verification and registration time are not on it. */
    @Transactional
    public void update(Integer id, String lastName, String firstName, String phone, String email) {
        Customer c = customers.findById(id).orElseThrow(() -> new BusinessException("Клієнта не знайдено."));
        c.setLastName(lastName.trim());
        c.setFirstName(firstName.trim());
        c.setPhone(phone.trim());
        c.setEmail(blankToNull(email));
        customers.flush();
    }

    /** BR19: simulated Diia check, sets the time once and never clears it. */
    @Transactional
    public void verifyWithDiia(Integer id) {
        Customer c = customers.findById(id).orElseThrow(() -> new BusinessException("Клієнта не знайдено."));
        if (c.isVerified()) {
            throw new BusinessException("Клієнта вже підтверджено через Дію.");
        }
        LocalDateTime now = ClockConfig.now(clock);
        int rows = customers.markVerified(id, now.isBefore(c.getRegisteredAt()) ? c.getRegisteredAt() : now);
        if (rows != 1) {
            throw new BusinessException("Клієнта вже підтверджено через Дію.");
        }
    }

    /** BR16, D28: only a customer without rides and without payments other than the bonus; the bonus goes with them. */
    @Transactional
    public void delete(Integer id) {
        Customer c = customers.findById(id).orElseThrow(() -> new BusinessException("Клієнта не знайдено."));
        long rideCount = rides.countByCustomerId(id);
        if (rideCount > 0) {
            throw new BusinessException("Клієнта не можна видалити: у нього є поїздки (" + rideCount + ").");
        }
        long paymentCount = payments.countByCustomerIdAndKindNot(id, PaymentKind.BONUS);
        if (paymentCount > 0) {
            throw new BusinessException("Клієнта не можна видалити: у нього є платежі, крім бонусу за реєстрацію (" + paymentCount + ").");
        }
        // entity remove (allowed on @Immutable), bonus row first, then the customer
        payments.findByCustomerIdAndKind(id, PaymentKind.BONUS).forEach(payments::delete);
        payments.flush();
        customers.delete(c);
        customers.flush();
    }
}

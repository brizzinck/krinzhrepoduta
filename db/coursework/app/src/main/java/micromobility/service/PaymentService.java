package micromobility.service;

import java.time.Clock;
import java.util.List;
import micromobility.config.ClockConfig;
import micromobility.domain.Customer;
import micromobility.domain.Payment;
import micromobility.domain.PaymentKind;
import micromobility.domain.Ride;
import micromobility.repository.CustomerRepository;
import micromobility.repository.PaymentRepository;
import micromobility.repository.RideRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The wallet ledger is append-only (BR14): only inserts, never updates. */
@Service
public class PaymentService {

    public static final List<Integer> TOPUP_UAH = List.of(100, 200, 500);

    private final PaymentRepository payments;
    private final CustomerRepository customers;
    private final RideRepository rides;
    private final Clock clock;

    public PaymentService(PaymentRepository payments, CustomerRepository customers, RideRepository rides, Clock clock) {
        this.payments = payments;
        this.customers = customers;
        this.rides = rides;
        this.clock = clock;
    }

    public long balanceKop(Integer customerId) {
        return payments.balanceOf(customerId);
    }

    /** BR13: a top-up of 100, 200 or 500 UAH. */
    @Transactional
    public void topUp(Integer customerId, int uah) {
        if (!TOPUP_UAH.contains(uah)) {
            throw new BusinessException("Поповнення має бути на 100, 200 або 500 грн.");
        }
        Customer c = customers.findById(customerId).orElseThrow(() -> new BusinessException("Клієнта не знайдено."));
        payments.saveAndFlush(new Payment(c, null, PaymentKind.TOPUP, uah * 100, ClockConfig.now(clock)));
    }

    /**
     * BR20: money back for one finished, charged ride; up to the charge; once per ride. The customer is taken from
     * the ride (fk_payments_ride_customer), the stored commission of the ride is not touched.
     */
    @Transactional
    public void refund(Integer rideId, int amountKop) {
        Ride ride = rides.findForUpdateById(rideId).orElseThrow(() -> new BusinessException("Поїздку не знайдено."));
        if (ride.isActive()) {
            throw new BusinessException("Повернення можливе лише за завершену поїздку.");
        }
        Payment charge = payments.findByRideIdAndKind(rideId, PaymentKind.RIDE)
                .orElseThrow(() -> new BusinessException("За цю поїздку не було списання."));
        if (payments.findByRideIdAndKind(rideId, PaymentKind.REFUND).isPresent()) {
            throw new BusinessException("За цю поїздку повернення вже було.");
        }
        int max = -charge.getAmountKop();
        if (amountKop <= 0 || amountKop > max) {
            throw new BusinessException("Сума повернення має бути від 0,01 грн до суми списання ("
                    + Money.uah(max) + " грн).");
        }
        payments.saveAndFlush(new Payment(ride.getCustomer(), ride, PaymentKind.REFUND, amountKop,
                ClockConfig.now(clock)));
    }
}

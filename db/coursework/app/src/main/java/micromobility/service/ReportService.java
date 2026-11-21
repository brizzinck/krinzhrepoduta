package micromobility.service;

import java.time.LocalDate;
import java.util.List;
import micromobility.config.Times;
import micromobility.domain.PaymentKind;
import micromobility.domain.VehicleStatus;
import micromobility.domain.VehicleType;
import micromobility.report.AvailableVehicleRow;
import micromobility.report.BalanceRow;
import micromobility.report.PivotRow;
import micromobility.report.RevenueRow;
import micromobility.report.RideRow;
import micromobility.report.TopCustomerRow;
import micromobility.domain.Vehicle;
import micromobility.repository.CustomerRepository;
import micromobility.repository.PaymentRepository;
import micromobility.repository.RideRepository;
import micromobility.repository.VehicleRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reports R1-R7. Every filter is optional (empty = no filter); the two thresholds of R6 and R7 are required and
 * pre-filled by the controller. Dates come from the form as Kyiv dates and are turned into UTC times here.
 */
@Service
@Transactional(readOnly = true)
public class ReportService {

    private final VehicleRepository vehicles;
    private final RideRepository rides;
    private final PaymentRepository payments;
    private final CustomerRepository customers;

    public ReportService(VehicleRepository vehicles, RideRepository rides, PaymentRepository payments,
                         CustomerRepository customers) {
        this.vehicles = vehicles;
        this.rides = rides;
        this.payments = payments;
        this.customers = customers;
    }

    /** R1. The unlock minimum (10 %) always applies: the filter can only raise it. */
    public List<AvailableVehicleRow> r1(Integer opId, VehicleType type, Integer stationId, Integer minBattery) {
        int min = Math.max(RideService.MIN_BATTERY, minBattery == null ? 0 : minBattery);
        return vehicles.findAvailable(VehicleStatus.AVAILABLE, min, opId, type, stationId);
    }

    /** R2. q is a surname prefix or a phone suffix; active: true = active rides, false = finished, null = all. */
    public List<RideRow> r2(LocalDate from, LocalDate to, Integer opId, VehicleType type, String q, Boolean active) {
        String text = q == null || q.isBlank() ? null : q.trim();
        return rides.searchRides(PaymentKind.RIDE, PaymentKind.REFUND, Times.startUtc(from), Times.endUtc(to),
                opId, type, text == null ? null : Like.escape(text) + "%", text == null ? null : "%" + Like.escape(text), active);
    }

    public List<RevenueRow> r3(LocalDate from, LocalDate to) {
        return rides.revenue(PaymentKind.RIDE, PaymentKind.REFUND, Times.startUtc(from), Times.endUtc(to));
    }

    /** R4: the top 5 by net spend; minRides empty = 1. */
    public List<TopCustomerRow> r4(LocalDate from, LocalDate to, Integer minRides) {
        return payments.topCustomers(List.of(PaymentKind.RIDE, PaymentKind.REFUND), Times.startUtc(from),
                Times.endUtc(to), minRides == null ? 1 : minRides, PageRequest.of(0, 5));
    }

    public List<PivotRow> r5(LocalDate from, LocalDate to) {
        return rides.pivot(Times.startUtc(from), Times.endUtc(to));
    }

    /** R6: thresholdUah is in hryvnias. */
    public List<BalanceRow> r6(int thresholdUah) {
        return customers.findBelowBalance(PaymentKind.TOPUP, thresholdUah * 100L);
    }

    public List<Vehicle> r7(int level) {
        return vehicles.findByStatusAndBatteryPctLessThanOrderByBatteryPctAscCodeAsc(VehicleStatus.AVAILABLE, level);
    }

    /** Ids of the vehicles in R1 (no filters), for the start form. */
    public List<AvailableVehicleRow> startableVehicles() {
        return r1(null, null, null, null);
    }
}

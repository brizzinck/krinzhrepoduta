package micromobility.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import micromobility.config.ClockConfig;
import micromobility.domain.Customer;
import micromobility.domain.Payment;
import micromobility.domain.PaymentKind;
import micromobility.domain.Ride;
import micromobility.domain.Station;
import micromobility.domain.Tariff;
import micromobility.domain.Vehicle;
import micromobility.domain.VehicleStatus;
import micromobility.repository.CustomerRepository;
import micromobility.repository.PaymentRepository;
import micromobility.repository.RideRepository;
import micromobility.repository.StationRepository;
import micromobility.repository.TariffRepository;
import micromobility.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ride start, finish and cancel. Each method is one transaction that writes the ride, the vehicle status and
 * station (BR21, BR22) and, at the finish, the charge. Only this class writes vehicle status 2.
 */
@Service
public class RideService {

    /** BR8: the balance needed to unlock, 100 UAH. */
    public static final int MIN_BALANCE_KOP = 10000;
    /** BR7: the battery needed to unlock, percent. */
    public static final int MIN_BATTERY = 10;

    private final RideRepository rides;
    private final CustomerRepository customers;
    private final VehicleRepository vehicles;
    private final StationRepository stations;
    private final TariffRepository tariffs;
    private final PaymentRepository payments;
    private final Clock clock;

    public RideService(RideRepository rides, CustomerRepository customers, VehicleRepository vehicles,
                       StationRepository stations, TariffRepository tariffs, PaymentRepository payments, Clock clock) {
        this.rides = rides;
        this.customers = customers;
        this.vehicles = vehicles;
        this.stations = stations;
        this.tariffs = tariffs;
        this.payments = payments;
        this.clock = clock;
    }

    /** BR7-BR9, BR21: checks with clear messages first, then the atomic take of the vehicle, then the ride row. */
    @Transactional
    public Ride start(Integer customerId, Integer vehicleId) {
        Customer customer = customers.findById(customerId)
                .orElseThrow(() -> new BusinessException("Клієнта не знайдено."));
        Vehicle vehicle = vehicles.findById(vehicleId)
                .orElseThrow(() -> new BusinessException("Транспорт не знайдено."));
        if (!customer.isVerified()) {
            throw new BusinessException("Клієнт не підтверджений через Дію: поїздку розпочати не можна.");
        }
        if (rides.existsByCustomerIdAndEndedAtIsNull(customerId)) {
            throw new BusinessException("У клієнта вже є активна поїздка.");
        }
        long balance = payments.balanceOf(customerId);
        if (balance < MIN_BALANCE_KOP) {
            throw new BusinessException("Баланс клієнта " + Money.uah(balance)
                    + " грн: для розблокування потрібно щонайменше " + Money.uah(MIN_BALANCE_KOP) + " грн.");
        }
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new BusinessException("Транспорт недоступний: статус «" + vehicle.getStatus().getLabel() + "».");
        }
        if (vehicle.getBatteryPct() < MIN_BATTERY) {
            throw new BusinessException("Заряд транспорту " + vehicle.getBatteryPct()
                    + " %: для розблокування потрібно щонайменше " + MIN_BATTERY + " %.");
        }
        if (rides.existsByVehicleIdAndEndedAtIsNull(vehicleId)) {
            throw new BusinessException("Цей транспорт уже в активній поїздці.");
        }
        Tariff tariff = tariffs.findByOperatorIdAndVehicleType(vehicle.getOperator().getId(), vehicle.getVehicleType())
                .orElseThrow(() -> new BusinessException("У оператора немає тарифу для цього типу транспорту."));
        int unlockFee = tariff.getUnlockFeeKop();
        int perMinute = tariff.getPerMinuteKop();

        // the atomic take: status 1 to 2 and station cleared in one conditional UPDATE; it clears the persistence context
        int taken = vehicles.markInRide(vehicleId, VehicleStatus.AVAILABLE, VehicleStatus.IN_RIDE, MIN_BATTERY);
        if (taken != 1) {
            throw new BusinessException("Транспорт щойно зайняли або він більше не доступний.");
        }

        Ride ride = new Ride();
        ride.setCustomer(customers.getReferenceById(customerId));
        ride.setVehicle(vehicles.getReferenceById(vehicleId));
        ride.setStartedAt(ClockConfig.now(clock));
        ride.setUnlockFeeKop(unlockFee);
        ride.setPerMinuteKop(perMinute);
        return rides.saveAndFlush(ride);
    }

    /**
     * BR10, BR17, BR21, BR22: finish, charge, commission and return of the vehicle to a station, one transaction.
     * READ_COMMITTED: the station is locked and then counted, so the count must see the latest committed rows.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void finish(Integer rideId, Integer stationId) {
        Ride ride = rides.findForUpdateById(rideId).orElseThrow(() -> new BusinessException("Поїздку не знайдено."));
        if (!ride.isActive()) {
            throw new BusinessException("Поїздку вже завершено.");
        }
        Station station = lockedStationWithRoom(stationId);
        Integer vehicleId = ride.getVehicle().getId();
        BigDecimal pct = ride.getVehicle().getOperator().getCommissionPct();
        LocalDateTime startedAt = ride.getStartedAt();
        LocalDateTime endedAt = ClockConfig.now(clock);
        if (endedAt.isBefore(startedAt)) {
            endedAt = startedAt;
        }
        int minutes = Billing.minutes(startedAt, endedAt);
        int price = Billing.price(ride.getUnlockFeeKop(), ride.getPerMinuteKop(), minutes);
        int commission = Billing.commission(price, pct);

        ride.setEndedAt(endedAt);
        ride.setCommissionKop(commission);
        rides.saveAndFlush(ride);
        // the customer comes from the ride, as fk_payments_ride_customer requires; dated at the finish (copy f)
        payments.saveAndFlush(new Payment(ride.getCustomer(), ride, PaymentKind.RIDE, -price, endedAt));
        giveBackVehicle(vehicleId, station);
    }

    /** BR15: an active ride is deleted without a charge; the vehicle returns to a station. */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void cancel(Integer rideId, Integer stationId) {
        Ride ride = rides.findForUpdateById(rideId).orElseThrow(() -> new BusinessException("Поїздку не знайдено."));
        if (!ride.isActive()) {
            throw new BusinessException("Завершену поїздку скасувати не можна: це фінансовий документ.");
        }
        Station station = lockedStationWithRoom(stationId);
        Integer vehicleId = ride.getVehicle().getId();
        rides.delete(ride);
        rides.flush();
        giveBackVehicle(vehicleId, station);
    }

    /** BR22: the station is locked (SELECT ... FOR UPDATE) so two finishes cannot both take the last free place. */
    private Station lockedStationWithRoom(Integer stationId) {
        if (stationId == null) {
            throw new BusinessException("Оберіть станцію, де залишено транспорт.");
        }
        Station station = stations.findForUpdateById(stationId)
                .orElseThrow(() -> new BusinessException("Станцію не знайдено."));
        if (vehicles.countByStationId(stationId) >= station.getCapacity()) {
            throw new BusinessException("Станція «" + station.getName() + "» заповнена (місткість "
                    + station.getCapacity() + ").");
        }
        return station;
    }

    private void giveBackVehicle(Integer vehicleId, Station station) {
        int rows = vehicles.markAvailable(vehicleId, station, VehicleStatus.AVAILABLE, VehicleStatus.IN_RIDE);
        if (rows != 1) {
            throw new BusinessException("Статус транспорту не збігається з активною поїздкою.");
        }
    }
}

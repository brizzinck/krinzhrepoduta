package micromobility.service;

import micromobility.domain.Operator;
import micromobility.domain.Station;
import micromobility.domain.Vehicle;
import micromobility.domain.VehicleStatus;
import micromobility.repository.OperatorRepository;
import micromobility.repository.RideRepository;
import micromobility.repository.StationRepository;
import micromobility.repository.TariffRepository;
import micromobility.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/** Vehicle CRUD. Status 2 (IN_RIDE) is written only by RideService (BR21); here it is neither offered nor changed. */
@Service
public class VehicleService {

    private final VehicleRepository vehicles;
    private final OperatorRepository operators;
    private final StationRepository stations;
    private final TariffRepository tariffs;
    private final RideRepository rides;

    public VehicleService(VehicleRepository vehicles, OperatorRepository operators, StationRepository stations,
                          TariffRepository tariffs, RideRepository rides) {
        this.vehicles = vehicles;
        this.operators = operators;
        this.stations = stations;
        this.tariffs = tariffs;
        this.rides = rides;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void create(VehicleData d) {
        Operator operator = checkCommon(d);
        Station station = stationFor(d, null);
        Vehicle v = new Vehicle();
        v.setCode(d.code().trim());
        v.setOperator(operator);
        v.setVehicleType(d.vehicleType());
        v.setModel(d.model().trim());
        v.setBatteryPct(d.batteryPct());
        v.setStatus(d.status());
        v.setStation(station);
        vehicles.saveAndFlush(v);
    }

    /**
     * BR18: operator and type are locked once the vehicle has rides. BR21: a vehicle in a ride is not edited, and the
     * conditional UPDATE never overwrites a vehicle that a ride took while the form was open.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void update(Integer id, VehicleData d) {
        Vehicle v = vehicles.findById(id).orElseThrow(() -> new BusinessException("Транспорт не знайдено."));
        if (v.getStatus() == VehicleStatus.IN_RIDE) {
            throw new BusinessException("Транспорт у поїздці: змінити його можна після завершення поїздки.");
        }
        boolean keyChanged = !v.getOperator().getId().equals(d.operatorId()) || v.getVehicleType() != d.vehicleType();
        if (keyChanged && rides.existsByVehicleId(id)) {
            throw new BusinessException("Оператора і тип змінити не можна: у транспорту вже були поїздки.");
        }
        Operator operator = checkCommon(d);
        Integer currentStationId = v.getStation() == null ? null : v.getStation().getId();
        Station station = stationFor(d, currentStationId);
        int rows = vehicles.updateFromForm(id, d.code().trim(), operator, d.vehicleType(), d.model().trim(),
                d.batteryPct(), d.status(), station, VehicleStatus.IN_RIDE);
        if (rows != 1) {
            throw new BusinessException("Транспорт щойно взяли в поїздку: зміни не збережено.");
        }
    }

    /** BR16: a vehicle with rides is not deleted; it is taken out of use with status RETIRED. */
    @Transactional
    public void delete(Integer id) {
        Vehicle v = vehicles.findById(id).orElseThrow(() -> new BusinessException("Транспорт не знайдено."));
        long rideCount = rides.countByVehicleId(id);
        if (rideCount > 0) {
            throw new BusinessException("Транспорт не можна видалити: за ним є поїздки (" + rideCount + "). Змініть статус на «Списано», історія збережеться.");
        }
        vehicles.delete(v);
        vehicles.flush();
    }

    /** The operator exists, has a tariff for the type (BR7, clear message before fk_vehicles_tariff), status is allowed. */
    private Operator checkCommon(VehicleData d) {
        if (d.status() == VehicleStatus.IN_RIDE) {
            throw new BusinessException("Статус «У поїздці» встановлює лише початок поїздки.");
        }
        Operator operator = operators.findById(d.operatorId())
                .orElseThrow(() -> new BusinessException("Оператора не знайдено."));
        if (tariffs.findByOperatorIdAndVehicleType(d.operatorId(), d.vehicleType()).isEmpty()) {
            throw new BusinessException("У оператора «" + operator.getName() + "» немає тарифу для типу «"
                    + d.vehicleType().getLabel() + "». Спершу створіть тариф.");
        }
        return operator;
    }

    /** BR22: AVAILABLE needs a station with a free place; any other status has no station. */
    private Station stationFor(VehicleData d, Integer currentStationId) {
        if (d.status() != VehicleStatus.AVAILABLE) {
            return null;
        }
        if (d.stationId() == null) {
            throw new BusinessException("Для статусу «Чекає клієнта» оберіть станцію.");
        }
        Station station = stations.findForUpdateById(d.stationId())
                .orElseThrow(() -> new BusinessException("Станцію не знайдено."));
        long parked = vehicles.countByStationId(station.getId());
        boolean alreadyThere = station.getId().equals(currentStationId);
        if (!alreadyThere && parked >= station.getCapacity()) {
            throw new BusinessException("Станція «" + station.getName() + "» заповнена (місткість "
                    + station.getCapacity() + ").");
        }
        return station;
    }
}

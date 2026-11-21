package micromobility.service;

import micromobility.domain.Operator;
import micromobility.domain.Station;
import micromobility.domain.Tariff;
import micromobility.domain.VehicleType;
import micromobility.repository.OperatorRepository;
import micromobility.repository.StationRepository;
import micromobility.repository.TariffRepository;
import micromobility.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/** CRUD of the reference entities: operators, tariffs, stations, with the delete and edit rules BR5, BR16, BR22. */
@Service
public class CatalogService {

    private final OperatorRepository operators;
    private final TariffRepository tariffs;
    private final StationRepository stations;
    private final VehicleRepository vehicles;

    public CatalogService(OperatorRepository operators, TariffRepository tariffs, StationRepository stations,
                          VehicleRepository vehicles) {
        this.operators = operators;
        this.tariffs = tariffs;
        this.stations = stations;
        this.vehicles = vehicles;
    }

    // ---- operators ----

    @Transactional
    public void saveOperator(Integer id, Operator form) {
        Operator o = id == null ? new Operator()
                : operators.findById(id).orElseThrow(() -> new BusinessException("Оператора не знайдено."));
        o.setName(form.getName().trim());
        o.setPhone(form.getPhone().trim());
        o.setEmail(form.getEmail().trim());
        o.setCommissionPct(form.getCommissionPct());
        operators.saveAndFlush(o);
    }

    /** BR16: an operator with tariffs or vehicles cannot be deleted. */
    @Transactional
    public void deleteOperator(Integer id) {
        Operator o = operators.findById(id).orElseThrow(() -> new BusinessException("Оператора не знайдено."));
        long tariffCount = tariffs.countByOperatorId(id);
        if (tariffCount > 0) {
            throw new BusinessException("Оператора не можна видалити: у нього є тарифи (" + tariffCount
                    + "). Спершу видаліть тарифи.");
        }
        long vehicleCount = vehicles.countByOperatorId(id);
        if (vehicleCount > 0) {
            throw new BusinessException("Оператора не можна видалити: у нього є транспорт (" + vehicleCount + ").");
        }
        operators.delete(o);
        operators.flush();
    }

    // ---- tariffs ----

    /** BR5: one tariff per operator and type; the pair cannot change while vehicles use it (prices can). */
    @Transactional
    public void saveTariff(Integer id, Integer operatorId, VehicleType type, int unlockFeeKop, int perMinuteKop) {
        Operator operator = operators.findById(operatorId)
                .orElseThrow(() -> new BusinessException("Оператора не знайдено."));
        Tariff t;
        if (id == null) {
            if (tariffs.findByOperatorIdAndVehicleType(operatorId, type).isPresent()) {
                throw new BusinessException("Для цього оператора і типу транспорту тариф уже є.");
            }
            t = new Tariff();
        } else {
            t = tariffs.findById(id).orElseThrow(() -> new BusinessException("Тариф не знайдено."));
            boolean pairChanged = !t.getOperator().getId().equals(operatorId) || t.getVehicleType() != type;
            if (pairChanged && vehicles.existsByOperatorIdAndVehicleType(t.getOperator().getId(), t.getVehicleType())) {
                throw new BusinessException("Оператора і тип змінити не можна: за цим тарифом є транспорт. Можна змінити ціни.");
            }
        }
        t.setOperator(operator);
        t.setVehicleType(type);
        t.setUnlockFeeKop(unlockFeeKop);
        t.setPerMinuteKop(perMinuteKop);
        tariffs.saveAndFlush(t);
    }

    @Transactional
    public void deleteTariff(Integer id) {
        Tariff t = tariffs.findById(id).orElseThrow(() -> new BusinessException("Тариф не знайдено."));
        long used = vehicles.countByOperatorIdAndVehicleType(t.getOperator().getId(), t.getVehicleType());
        if (used > 0) {
            throw new BusinessException("Тариф не можна видалити: за ним є транспорт цього оператора і типу (" + used + ").");
        }
        tariffs.delete(t);
        tariffs.flush();
    }

    // ---- stations ----

    /** BR22: the capacity cannot be lowered below the number of vehicles standing at the station. */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void saveStation(Integer id, Station form) {
        Station s;
        if (id == null) {
            s = new Station();
        } else {
            s = stations.findForUpdateById(id).orElseThrow(() -> new BusinessException("Станцію не знайдено."));
            long parked = vehicles.countByStationId(id);
            if (form.getCapacity() != null && form.getCapacity() < parked) {
                throw new BusinessException("Місткість не може бути меншою за кількість транспорту на станції (" + parked + ").");
            }
        }
        s.setName(form.getName().trim());
        s.setAddress(form.getAddress().trim());
        s.setCapacity(form.getCapacity());
        stations.saveAndFlush(s);
    }

    @Transactional
    public void deleteStation(Integer id) {
        Station s = stations.findById(id).orElseThrow(() -> new BusinessException("Станцію не знайдено."));
        long parked = vehicles.countByStationId(id);
        if (parked > 0) {
            throw new BusinessException("Станцію не можна видалити: на ній стоїть транспорт (" + parked + ").");
        }
        stations.delete(s);
        stations.flush();
    }
}

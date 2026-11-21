package micromobility.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import micromobility.domain.Station;
import micromobility.report.StationLoad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface StationRepository extends JpaRepository<Station, Integer> {

    List<Station> findAllByOrderByNameAsc();

    /** SELECT ... FOR UPDATE: finishes at one station are serialized, so the capacity check (BR22) is exact. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Station> findForUpdateById(Integer id);

    /** Stations with the number of parked vehicles. */
    @Query("""
            select new micromobility.report.StationLoad(s.id, s.name, s.address, s.capacity, count(v))
            from Station s left join Vehicle v on v.station = s
            group by s.id, s.name, s.address, s.capacity
            order by s.name, s.id""")
    List<StationLoad> loads();
}

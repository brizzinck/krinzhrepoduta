package micromobility.repository;

import java.util.List;
import micromobility.domain.Operator;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperatorRepository extends JpaRepository<Operator, Integer> {

    List<Operator> findAllByOrderByNameAsc();
}

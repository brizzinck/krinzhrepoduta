package micromobility.report;

import java.math.BigDecimal;

/** R5 row (native query, interface projection). isTotal = 1 marks the WITH ROLLUP total row. */
public interface PivotRow {
    String getOperator();
    Long getIsTotal();
    Long getScooterRides();
    BigDecimal getScooterAvgMin();
    Long getBikeRides();
    BigDecimal getBikeAvgMin();
    Long getTotalRides();
}

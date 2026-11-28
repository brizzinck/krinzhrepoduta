package rental;

import java.math.BigDecimal;

/** Результат запиту з агрегатними функціями: виручка філії. */
public record BranchRevenue(String branch, long rentals, long payments, BigDecimal total, BigDecimal avgPayment) {
}

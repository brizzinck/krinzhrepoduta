package ua.duikt.lavrishko.importer.app;

import ua.duikt.lavrishko.importer.domain.StockItem;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Накопичує підсумок під час читання: лічильники й суму, а не самі записи.
 * Тому пам'ять не залежить від розміру файлу.
 */
public final class ImportSummary {

    private long imported;
    private long rejected;
    private BigDecimal totalValue = BigDecimal.ZERO;

    void accept(StockItem item) {
        imported++;
        totalValue = totalValue.add(item.totalValue());
    }

    void reject() {
        rejected++;
    }

    public long imported() {
        return imported;
    }

    public long rejected() {
        return rejected;
    }

    public long dataLines() {
        return imported + rejected;
    }

    public BigDecimal totalValue() {
        return totalValue.setScale(2, RoundingMode.HALF_UP);
    }
}

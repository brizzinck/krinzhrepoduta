package ua.duikt.lavrishko.importer.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Залишок товару на складі (варіант 2): артикул, кількість, ціна за одиницю.
 * Об'єкт незмінний. Конструктор закритий, тому створити його можна лише через create(),
 * яка перевіряє всі правила. Некоректний StockItem у програмі існувати не може.
 */
public final class StockItem {

    private final String article;
    private final int quantity;
    private final BigDecimal price;

    private StockItem(String article, int quantity, BigDecimal price) {
        this.article = article;
        this.quantity = quantity;
        this.price = price;
    }

    public static StockItem create(String article, int quantity, BigDecimal price)
            throws InvalidStockItemException {
        if (article == null || article.isBlank()) {
            throw new InvalidStockItemException("артикул порожній");
        }
        if (quantity <= 0) {
            throw new InvalidStockItemException("кількість " + quantity + " має бути більше 0");
        }
        Objects.requireNonNull(price, "price");
        if (price.signum() < 0) {
            throw new InvalidStockItemException("ціна " + price.toPlainString() + " не може бути від'ємною");
        }
        return new StockItem(article.strip(), quantity, price);
    }

    public String article() {
        return article;
    }

    public int quantity() {
        return quantity;
    }

    public BigDecimal price() {
        return price;
    }

    /** Вартість залишку цієї позиції: кількість × ціна. */
    public BigDecimal totalValue() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}

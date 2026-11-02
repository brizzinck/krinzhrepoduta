package ua.duikt.lavrishko.importer.parse;

/**
 * Рядок вхідного файлу не можна перетворити на StockItem.
 * Зберігає номер рядка у файлі. Низькорівнева причина (NumberFormatException,
 * InvalidStockItemException) передається в cause і не губиться.
 */
public class RecordFormatException extends Exception {

    private final long lineNumber;

    public RecordFormatException(long lineNumber, String message) {
        super(message);
        this.lineNumber = lineNumber;
    }

    public RecordFormatException(long lineNumber, String message, Throwable cause) {
        super(message, cause);
        this.lineNumber = lineNumber;
    }

    public long lineNumber() {
        return lineNumber;
    }
}

package ua.duikt.lavrishko.importer.parse;

import ua.duikt.lavrishko.importer.domain.InvalidStockItemException;
import ua.duikt.lavrishko.importer.domain.StockItem;

import java.math.BigDecimal;

/**
 * Межа між текстом файлу і предметною областю: розбирає рядок "артикул;кількість;ціна".
 * Усі перевірки відбуваються тут, під час розбору. Далі в програму потрапляють лише коректні об'єкти.
 */
public final class StockItemParser {

    public static final String HEADER = "артикул;кількість;ціна";
    private static final int FIELD_COUNT = 3;

    public StockItem parse(String line, long lineNumber) throws RecordFormatException {
        if (line.isBlank()) {
            throw new RecordFormatException(lineNumber, "порожній рядок");
        }
        // limit = -1: не відкидати порожні поля в кінці ("A-1;5;" дає 3 поля, а не 2)
        String[] fields = line.split(";", -1);
        if (fields.length != FIELD_COUNT) {
            throw new RecordFormatException(lineNumber,
                    "очікувалось " + FIELD_COUNT + " поля, отримано " + fields.length);
        }
        String article = fields[0].strip();
        int quantity = parseQuantity(fields[1].strip(), lineNumber);
        BigDecimal price = parsePrice(fields[2].strip(), lineNumber);
        try {
            return StockItem.create(article, quantity, price);
        } catch (InvalidStockItemException e) {
            // повідомлення доменного винятку зрозуміле користувачу, а сам виняток лишається причиною
            throw new RecordFormatException(lineNumber, e.getMessage(), e);
        }
    }

    private int parseQuantity(String text, long lineNumber) throws RecordFormatException {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new RecordFormatException(lineNumber,
                    "кількість \"" + text + "\" не є цілим числом", e);
        }
    }

    private BigDecimal parsePrice(String text, long lineNumber) throws RecordFormatException {
        if (text.indexOf(',') >= 0) {
            throw new RecordFormatException(lineNumber,
                    "ціна \"" + text + "\": дробову частину треба відокремлювати крапкою");
        }
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            throw new RecordFormatException(lineNumber,
                    "ціна \"" + text + "\" не є числом", e);
        }
    }
}

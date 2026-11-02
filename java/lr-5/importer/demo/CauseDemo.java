import ua.duikt.lavrishko.importer.parse.RecordFormatException;
import ua.duikt.lavrishko.importer.parse.StockItemParser;

/**
 * Показує ланцюжок причин для відхилених рядків. Для користувача утиліта друкує лише повідомлення,
 * а тут видно, що низькорівнева причина збережена в cause.
 */
public class CauseDemo {
    public static void main(String[] args) {
        StockItemParser parser = new StockItemParser();
        String[] lines = {"A-1;десять;5", "A-1;0;5", "A-1;5;-1", "A-1;5;12,50"};
        for (int i = 0; i < lines.length; i++) {
            try {
                parser.parse(lines[i], i + 2);
            } catch (RecordFormatException e) {
                System.out.println("Вхід: " + lines[i]);
                System.out.println("  повідомлення: " + e.getMessage());
                Throwable cause = e.getCause();
                if (cause == null) {
                    System.out.println("  причина: немає (помилку виявлено самим парсером)");
                }
                while (cause != null) {
                    System.out.println("  причина: " + cause.getClass().getSimpleName() + ": " + cause.getMessage());
                    cause = cause.getCause();
                }
            }
        }
        System.out.println();
        System.out.println("Ланцюжок винятків для першого рядка (клас, повідомлення, верхній кадр стека):");
        try {
            parser.parse("A-1;десять;5", 2);
        } catch (RecordFormatException e) {
            for (Throwable t = e; t != null; t = t.getCause()) {
                System.out.println((t == e ? "" : "Caused by: ") + t.getClass().getSimpleName() + ": " + t.getMessage());
                StackTraceElement top = t.getStackTrace()[0];
                String cls = top.getClassName().substring(top.getClassName().lastIndexOf('.') + 1);
                System.out.println("    at " + cls + "." + top.getMethodName()
                        + "(" + top.getFileName() + ":" + top.getLineNumber() + ")");
            }
        }
    }
}

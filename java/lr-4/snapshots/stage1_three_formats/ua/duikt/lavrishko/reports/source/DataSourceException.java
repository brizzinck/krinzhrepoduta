package ua.duikt.lavrishko.reports.source;

/** Помилка читання даних із джерела. */
public class DataSourceException extends RuntimeException {
    public DataSourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataSourceException(String message) {
        super(message);
    }
}

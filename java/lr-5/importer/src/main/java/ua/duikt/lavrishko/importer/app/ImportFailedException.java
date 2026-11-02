package ua.duikt.lavrishko.importer.app;

/**
 * Імпорт неможливо продовжити: немає вхідного файлу, не вдається прочитати або записати.
 * Повідомлення пишемо для користувача. Технічна причина (IOException) лишається в cause.
 */
public class ImportFailedException extends Exception {

    public ImportFailedException(String message) {
        super(message);
    }

    public ImportFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}

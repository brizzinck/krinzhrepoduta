package ua.duikt.lavrishko.media;

/**
 * Базовий тип медіаконтенту. Спільні дані (назва, тривалість) і спільна поведінка
 * (форматування тривалості, склад опису) зберігаються тут, у похідних лише специфічне.
 */
public abstract class MediaContent {

    private final String title;
    private final int durationSeconds;

    /** Інваріанти: назва не порожня, тривалість додатна. Перевіряються лише тут, один раз. */
    protected MediaContent(String title, int durationSeconds) {
        this.title = requireText(title, "назва");
        if (durationSeconds <= 0) {
            throw new IllegalArgumentException("тривалість має бути більшою за 0, отримано " + durationSeconds);
        }
        this.durationSeconds = durationSeconds;
    }

    /** Спільна перевірка текстових полів для базового та похідних класів. */
    protected static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("поле «" + fieldName + "» не може бути порожнім");
        }
        return value.strip();
    }

    public final String title() {
        return title;
    }

    public final int durationSeconds() {
        return durationSeconds;
    }

    /** Тривалість у форматі ГГ:ХХ:СС. */
    public final String formattedDuration() {
        return formatSeconds(durationSeconds);
    }

    public static String formatSeconds(long seconds) {
        return String.format("%02d:%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60);
    }

    /** Вид контенту, наприклад «Фільм». Кожен різновид задає свій. */
    public abstract String kind();

    /** Специфічні подробиці різновиду. */
    protected abstract String details();

    /**
     * Спільна операція «опис». Метод final: склад опису (вид, назва, тривалість, подробиці)
     * однаковий для всіх різновидів і не може бути порушений похідними класами.
     */
    public final String describe() {
        return kind() + " «" + title + "», " + formattedDuration() + ". " + details();
    }
}

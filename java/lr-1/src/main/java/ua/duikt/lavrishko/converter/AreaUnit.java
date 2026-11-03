package ua.duikt.lavrishko.converter;

import java.util.Locale;

/** Одиниці площі. Коефіцієнт показує, скільки квадратних метрів в одній одиниці. */
public enum AreaUnit {
    CM2("cm²", "cm2", 0.0001),
    M2("m²", "m2", 1.0),
    ARE("a", "a", 100.0),
    HECTARE("ha", "ha", 10_000.0),
    // Міжнародний акр: 43560 квадратних футів, 1 фут = 0,3048 м.
    ACRE("acre", "acre", 4046.8564224);

    private final String label;
    private final String asciiName;
    private final double squareMetres;

    AreaUnit(String label, String asciiName, double squareMetres) {
        this.label = label;
        this.asciiName = asciiName;
        this.squareMetres = squareMetres;
    }

    public String label() {
        return label;
    }

    public double squareMetres() {
        return squareMetres;
    }

    /** Пошук за номером у меню (від 1). Для неіснуючого номера повертає null. */
    public static AreaUnit byNumber(int number) {
        AreaUnit[] units = values();
        return number >= 1 && number <= units.length ? units[number - 1] : null;
    }

    /** Пошук за назвою (cm², cm2, a, ha, acre). Для невідомої назви повертає null. */
    public static AreaUnit byName(String name) {
        String wanted = name.toLowerCase(Locale.ROOT);
        for (AreaUnit unit : values()) {
            if (unit.label.equals(wanted) || unit.asciiName.equals(wanted)) {
                return unit;
            }
        }
        return null;
    }
}

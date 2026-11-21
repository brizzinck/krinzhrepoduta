package micromobility.domain;

/** payments.kind: TINYINT UNSIGNED code (CHECK 1-4). */
public enum PaymentKind {
    BONUS(1, "Бонус за реєстрацію"),
    TOPUP(2, "Поповнення"),
    RIDE(3, "Оплата поїздки"),
    REFUND(4, "Повернення за поїздку");

    private final int code;
    private final String label;

    PaymentKind(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static PaymentKind fromCode(int code) {
        for (PaymentKind k : values()) {
            if (k.code == code) {
                return k;
            }
        }
        throw new IllegalArgumentException("Unknown payment kind code: " + code);
    }
}

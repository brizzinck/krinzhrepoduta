package micromobility.service;

/** Search text for LIKE: % and _ typed by the user are plain characters (escape character is !). */
public final class Like {

    private Like() {
    }

    public static String escape(String text) {
        return text.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }
}

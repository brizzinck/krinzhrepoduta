import ua.duikt.lavrishko.media.MediaContent;

// Спроба змінити формат опису в новому різновиді, що порушило б спільну угоду базового типу
class BadMedia extends MediaContent {
    BadMedia() {
        super("Назва", 100);
    }

    @Override
    public String kind() {
        return "Інше";
    }

    @Override
    protected String details() {
        return "подробиці";
    }

    @Override
    public String describe() {
        return "інший формат";
    }
}

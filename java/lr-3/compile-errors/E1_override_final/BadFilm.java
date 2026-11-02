import ua.duikt.lavrishko.media.Film;

// Спроба змінити формат опису в похідному класі, що порушило б спільну угоду базового типу
class BadFilm extends Film {
    BadFilm() {
        super("Назва", 100, "Режисер", 2020);
    }

    @Override
    public String describe() {
        return "інший формат";
    }
}

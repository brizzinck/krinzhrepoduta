import ua.duikt.lavrishko.media.Film;

// Спроба успадкуватися від готового різновиду й перевизначити його частини
class BadFilm extends Film {
    BadFilm() {
        super("Назва", 100, "Режисер", 2020);
    }

    @Override
    public String kind() {
        return "";
    }
}

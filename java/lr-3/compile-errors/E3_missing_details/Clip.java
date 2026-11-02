import ua.duikt.lavrishko.media.MediaContent;

// Новий різновид без реалізації абстрактних kind() і details()
class Clip extends MediaContent {
    Clip() {
        super("Кліп", 90);
    }
}

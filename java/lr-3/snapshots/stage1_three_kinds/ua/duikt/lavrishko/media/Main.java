package ua.duikt.lavrishko.media;

/** Точка складання: створює об'єкти різних різновидів і передає їх обробнику. */
public class Main {

    public static void main(String[] args) {
        MediaContent[] library = {
                new Film("Довга дорога додому", 8160, "Андрій Мельник", 2019),
                new Song("Тихий вечір", 214, "Гурт «Береги»", "Світанок"),
                new Podcast("Код і кава", 2745, "Олена Ковальчук", 12)
        };

        LibraryReport report = new LibraryReport();
        System.out.println("Бібліотека:");
        report.printAll(library);
        report.printSummary(library);

        System.out.println("Некоректні дані не створюють об'єкт:");
        try {
            new Film("", 100, "Режисер", 2020);
        } catch (IllegalArgumentException e) {
            System.out.println("Відхилено: " + e.getMessage());
        }
        try {
            new Song("Нуль", 0, "Виконавець", "Альбом");
        } catch (IllegalArgumentException e) {
            System.out.println("Відхилено: " + e.getMessage());
        }
    }
}

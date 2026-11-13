package ua.duikt.lavrishko.pr7;

import java.util.ArrayList;
import java.util.List;

/** Список з 9 задач: частина має не менше 5 story points, частина менше; є однакові оцінки. */
public final class Sample {
    private Sample() {
    }

    public static List<Issue> issues() {
        List<Issue> list = new ArrayList<>();
        list.add(new Issue("SHOP-101", "Вхід через Google", 5));
        list.add(new Issue("SHOP-102", "Експорт замовлень у CSV", 8));
        list.add(new Issue("SHOP-103", "Темна тема інтерфейсу", 3));
        list.add(new Issue("SHOP-104", "Пагінація в пошуку", 13));
        list.add(new Issue("SHOP-105", "Кешування каталогу", 2));
        list.add(new Issue("SHOP-106", "Rate limiting для API", 5));
        list.add(new Issue("SHOP-107", "Міграція таблиці users", 1));
        list.add(new Issue("SHOP-108", "Метрики часу відповіді", 8));
        list.add(new Issue("SHOP-109", "Docker-образ для сервісу", 3));
        return list;
    }
}

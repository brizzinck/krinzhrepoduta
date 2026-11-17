package ua.duikt.lavrishko.lr6;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Сховище записів із ключем. Працює з будь-яким типом T, що має ключ типу K.
 * Усередині LinkedHashMap: пошук за ключем за O(1), без дублікатів, порядок як у файлі.
 */
public class Storage<K, T extends Keyed<K>> implements Iterable<T> {
    private final Map<K, T> records = new LinkedHashMap<>();
    private final List<Replacement<T>> replacements = new ArrayList<>();

    /**
     * Додає запис. Якщо ключ уже є, виграє пізніший запис: він замінює попередній,
     * а заміна запам'ятовується (її можна показати користувачу).
     */
    public void add(T record) {
        T previous = records.put(record.key(), record);
        if (previous != null) {
            replacements.add(new Replacement<>(previous, record));
        }
    }

    /** Пошук за ключем без перебору. Відсутній ключ дає порожній Optional, не null. */
    public Optional<T> find(K key) {
        return Optional.ofNullable(records.get(key));
    }

    public int size() {
        return records.size();
    }

    /** Подання тільки для читання: бачить зміни сховища, але змінити його не дозволяє. */
    public Collection<T> all() {
        return Collections.unmodifiableCollection(records.values());
    }

    /** Повтори ключів, які сталися під час додавання (список лише для читання). */
    public List<Replacement<T>> replacements() {
        return Collections.unmodifiableList(replacements);
    }

    /**
     * Нова відсортована копія. List.sort стабільний і нічого не відкидає:
     * записи з однаковим значенням критерію не губляться; їхній порядок задає компаратор.
     */
    public List<T> sorted(Comparator<? super T> comparator) {
        List<T> copy = new ArrayList<>(records.values());
        copy.sort(comparator);
        return copy;
    }

    @Override
    public Iterator<T> iterator() {
        return all().iterator();
    }
}

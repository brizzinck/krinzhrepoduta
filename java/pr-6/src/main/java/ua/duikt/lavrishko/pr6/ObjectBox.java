package ua.duikt.lavrishko.pr6;

/** Той самий контейнер без узагальнень: зберігає Object, при читанні потрібне приведення типу. */
public class ObjectBox {
    private Object value;

    public void put(Object value) {
        this.value = value;
    }

    public Object get() {
        return value;
    }
}

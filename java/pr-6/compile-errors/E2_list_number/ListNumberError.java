import java.util.List;

public class ListNumberError {
    // Початкове оголошення параметра: рівно List<Number>
    static double total(List<Number> numbers) {
        double sum = 0;
        for (Number n : numbers) {
            sum += n.doubleValue();
        }
        return sum;
    }

    public static void main(String[] args) {
        List<Integer> ints = List.of(5, 8, 13);
        System.out.println(total(ints));
    }
}

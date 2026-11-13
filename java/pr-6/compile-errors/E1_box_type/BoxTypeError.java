import ua.duikt.lavrishko.pr6.Box;
import ua.duikt.lavrishko.pr6.Issue;

public class BoxTypeError {
    public static void main(String[] args) {
        Box<Issue> box = new Box<>();
        box.put("abc");   // рядок замість Issue
    }
}

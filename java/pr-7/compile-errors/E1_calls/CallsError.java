import java.util.ArrayList;
import java.util.List;
import ua.duikt.lavrishko.pr7.Issue;
import ua.duikt.lavrishko.pr7.Sample;

public class CallsError {
    public static void main(String[] args) {
        List<Issue> issues = new ArrayList<>(Sample.issues());
        int calls = 0;
        issues.sort((a, b) -> {
            calls++;   // спроба порахувати порівняння
            return Integer.compare(a.getStoryPoints(), b.getStoryPoints());
        });
        System.out.println("Порівнянь: " + calls);
    }
}

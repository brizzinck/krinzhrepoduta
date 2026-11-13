package ua.duikt.lavrishko.lr6;

import java.util.Comparator;

/** Критерій учасників: кількість закритих задач за зростанням, за однакових значень за логіном. */
public class ByClosedCount implements Comparator<Member> {
    @Override
    public int compare(Member a, Member b) {
        int byCount = Integer.compare(a.closedCount(), b.closedCount());
        return byCount != 0 ? byCount : a.login().compareTo(b.login());
    }
}

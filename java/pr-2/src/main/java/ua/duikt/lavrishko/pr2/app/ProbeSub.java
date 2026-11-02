package ua.duikt.lavrishko.pr2.app;

import ua.duikt.lavrishko.pr2.model.Account;

/**
 * Підклас Account в іншому пакеті (app). Через успадкування він бачить protected-член,
 * хоча Main (не підклас) у тому самому пакеті app його не бачить.
 */
public class ProbeSub extends Account {

    public ProbeSub() {
        super("Підклас", "UA-777777", 10.0);
    }

    public void show() {
        System.out.println("protected number з підкласу в app: " + this.number);
        // this.balance: НЕ компілюється, package-private не успадковується в інший пакет
    }
}

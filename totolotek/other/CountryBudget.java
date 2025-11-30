package totolotek.other;

import totolotek.finances.Money;

public class CountryBudget {
    private static CountryBudget singleton;
    private final Money taxCollected;
    private final Money subsidiesGiven;

    private CountryBudget() {
        taxCollected = new Money();
        subsidiesGiven = new Money();
    }

    public static CountryBudget getInstance() {
        if (singleton == null) {
            singleton = new CountryBudget();
        }
        return singleton;
    }

    public Money taxCollected() {
        return new Money(taxCollected);
    }

    public Money subsidiesGiven() {
        return new Money(subsidiesGiven);
    }

    public void collectTaxes(Money amount) {
        taxCollected.increase(amount);
    }

    public void giveSubsidies(Money amount) {
        subsidiesGiven.increase(amount);
    }

    @Override
    public String toString() {
        return "Państwo zebrało w podatkach: " + taxCollected().toString() +
                "\nPaństwo wydało w subwencjach: " + subsidiesGiven().toString() + "\n";
    }
}

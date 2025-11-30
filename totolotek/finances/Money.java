package totolotek.finances;

import totolotek.other.NumberHelper;

import java.util.Objects;

//We assume that there cannot be less than 0 money
public class Money implements Comparable<Money> {
    private long zloty;
    private long groszy;

    public Money() {
        zloty = 0;
        groszy = 0;
    }

    public Money(long zloty) {
        this();
        if (zloty < 0) {
            throw new IllegalArgumentException("There cannot be less than 0 cash!");
        }
        this.zloty = zloty;
    }

    public Money(long zloty, long groszy) {
        groszy += zloty * NumberHelper.GROSZY_PER_ZLOTY;
        if (groszy < 0) {
            throw new IllegalArgumentException("There cannot be less than 0 cash!");
        }
        this.groszy = groszy;
        normalize();
    }

    public Money(Money money) {
        this.zloty = money.zloty();
        this.groszy = money.groszy();
        normalize();
    }

    private void normalize() {
        //transfer all zloty to groszy
        groszy += zloty * NumberHelper.GROSZY_PER_ZLOTY;
        zloty = 0;
        //calculate zloty and groszy
        zloty += groszy / NumberHelper.GROSZY_PER_ZLOTY;
        groszy %= NumberHelper.GROSZY_PER_ZLOTY;
    }

    //a += b
    public void increase(Money money) {
        groszy += money.groszy();
        zloty += money.zloty();
        normalize();
    }

    //a + b
    public Money add(Money money) {
        Money result = new Money(this);
        result.zloty += money.zloty();
        result.groszy += money.groszy();
        result.normalize();
        return result;
    }

    //a - b
    public void decrease(Money amount) {
        amount.normalize();
        zloty -= amount.zloty();
        groszy -= amount.groszy();
        if (NumberHelper.GROSZY_PER_ZLOTY * zloty + groszy < 0) {
            throw new IllegalArgumentException("There cannot be less than 0 money!");
        }
        normalize();
        //the subtracted money as a new "Pile of money":
    }

    //a -= b
    public Money subtract(Money amount) {
        amount.normalize();
        Money result = new Money(this);
        result.decrease(amount);
        return result;
    }

    //a = 0
    public void setToZero() {
        zloty = 0;
        groszy = 0;
    }

    //take away some percent of the sum to another
    public Money percent(int percent) {
        return new Money(0, (zloty) * percent + groszy * percent / 100);
    }

    //floor(a / k) where k is natural
    public Money floor(int divisor) {
        if (divisor < 0) {
            throw new IllegalArgumentException("Negative divisor!");
        }
        return new Money(0, (zloty * NumberHelper.GROSZY_PER_ZLOTY + groszy) / divisor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(zloty, groszy);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Money money) {
            return money.groszy() == groszy && money.zloty() == zloty;
        }
        return false;
    }

    @Override
    public int compareTo(Money o) {
        o.normalize();
        normalize();
        if (zloty != o.zloty()) {
            return Long.compare(zloty, o.zloty());
        }
        return Long.compare(groszy, o.groszy());
    }

    @Override
    public String toString() {
        return zloty + " zł " + NumberHelper.fillWithCharToSize(Long.toString(groszy), 2, '0') + " gr";
    }

    public long zloty() {
        return zloty;
    }

    public long groszy() {
        return groszy;
    }
}

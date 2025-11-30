package totolotek.other;

import java.util.Objects;

//Note: equals is NOT the same as 0 returned by compareTo
public class Pair<T1 extends Comparable<T1>, T2> implements Comparable<Pair<T1, T2>> {
    private final T1 first;
    private final T2 second;

    public Pair(T1 obj1, T2 obj2) {
        first = obj1;
        second = obj2;
    }

    public T1 first() {
        return first;
    }

    public T2 second() {
        return second;
    }

    //This compareTo lets the second element of pair to be not-comparable
    @Override
    public int compareTo(Pair<T1, T2> o) {
        return first.compareTo(o.first);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pair<?, ?> pair) {
            return pair.first.equals(first) && pair.second.equals(second);
        }
        return false;
    }
}

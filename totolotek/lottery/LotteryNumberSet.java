package totolotek.lottery;

import totolotek.other.NumberHelper;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class LotteryNumberSet {

    //-1 represents CANCEL
    private final HashSet<Integer> values;

    //random valid bet
    public LotteryNumberSet() {
        values = new HashSet<>();
        for (int i = 0; i < NumberHelper.NUMBERS_IN_CORRECT_SET; i++) {
            int next = NumberHelper.random().nextInt(NumberHelper.MAX_LOTTERY_NUM) + 1;
            while (values.contains(next)) {
                next = NumberHelper.random().nextInt(NumberHelper.MAX_LOTTERY_NUM) + 1;
            }
            values.add(next);
        }
    }

    public LotteryNumberSet(int[] numbers) {
        values = new HashSet<>();
        for (int i : numbers) {
            values.add(i);
        }
    }

    //deep copy
    public LotteryNumberSet(LotteryNumberSet lotteryNumberSet) {
        values = new HashSet<>();
        //creates also copies of Integers
        for (int i : lotteryNumberSet.values()) {
            values.add(i);
        }
    }

    public LotteryNumberSet(Set<Integer> numbers) {
        values = new HashSet<>(numbers);
    }

    public Set<Integer> values() {
        return new HashSet<>(values);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Integer i : values) {
            builder.append(NumberHelper.fillWithCharToSize(Integer.toString(i), 2, ' '));
            builder.append(" ");
        }
        //remove last space
        builder.setLength(builder.length() - 1);
        return builder.toString();
    }

    public int compareSets(LotteryNumberSet set) {
        int counter = 0;
        for (int i : values) {
            if (set.values().contains(i)) {
                counter++;
            }
        }
        return counter;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LotteryNumberSet set) {
            return set.values().equals(values);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }
}

package totolotek.lottery;

import totolotek.other.NumberHelper;

import java.util.Objects;

public class DrawAmountSet {
    private final int value;

    public DrawAmountSet(int[] numbers) {
        int max = 0;
        for (int i : numbers) {
            if (i > NumberHelper.MAX_DRAW_AMOUNT_PER_BET || i < 1) {
                continue;
            }
            if (max < i) {
                max = i;
            }
        }
        value = max;
    }

    public int value() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DrawAmountSet set) {
            return set.value() == value;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}

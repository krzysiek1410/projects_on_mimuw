package totolotek.other;

import java.util.Random;

public final class NumberHelper {

    public static final int REWARD_TYPES = 4;
    public static final int NUMBERS_IN_CORRECT_SET = 6;
    public static final int MAX_LOTTERY_NUM = 50;
    public static final int MAX_DRAW_AMOUNT_PER_BET = 10;
    public static final int MAX_BETS_PER_FORM = 8;
    public static final long MIN_REWARD_FOR_TAX = 2280;
    public static final int GROSZY_PER_ZLOTY = 100;
    private static final Random random = new Random();

    //No object of this class is allowed...
    //I created here something working like un-nested static class
    private NumberHelper() {
    }

    public static String fillWithCharToSize(String s, int size, char c) {
        return Character.toString(c).repeat(Math.max(0, size - s.length())) + s;
    }

    public static int sumDigits(int number) {
        return Integer.toString(number).chars().sum() - '0' * Integer.toString(number).length();
    }

    public static String lastTwoDigits(int number) {
        return Integer.toString(number).substring(Integer.toString(number).length() - 2);
    }

    public static Random random() {
        return random;
    }
}

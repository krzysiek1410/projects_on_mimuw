package totolotek.lottery;

import totolotek.other.NumberHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Form {
    private final ArrayList<LotteryNumberSet> lotteryNumberSets;
    private final int drawAmount;

    public Form(Form form) {
        drawAmount = form.drawAmount();
        lotteryNumberSets = new ArrayList<>();
        for (int i = 0; i < form.bets().size(); i++) {
            lotteryNumberSets.add(new LotteryNumberSet(form.bets().get(i)));
        }
    }

    public Form(List<LotteryNumberSet> lotteryNumberSets, DrawAmountSet drawAmount) {
        validateParams(lotteryNumberSets.size(), drawAmount.value());
        this.lotteryNumberSets = new ArrayList<>(lotteryNumberSets);
        this.drawAmount = drawAmount.value();
    }

    public List<LotteryNumberSet> bets() {
        ArrayList<LotteryNumberSet> result = new ArrayList<>();
        for (LotteryNumberSet set : lotteryNumberSets) {
            result.add(new LotteryNumberSet(set));
        }
        return result;
    }

    public int drawAmount() {
        return drawAmount;
    }

    private void validateParams(int betAmount, int drawAmount) {
        if (betAmount > NumberHelper.MAX_BETS_PER_FORM) {
            throw new IllegalArgumentException("Cannot have more than " + NumberHelper.MAX_BETS_PER_FORM + " bets on a single Form!");
        }
        if (betAmount == 0) {
            throw new IllegalArgumentException("Cannot have 0 bets on a Form!");
        }
        if (drawAmount > NumberHelper.MAX_DRAW_AMOUNT_PER_BET) {
            throw new IllegalArgumentException("Cannot bet for more than " + NumberHelper.MAX_DRAW_AMOUNT_PER_BET + " draws on a single Form!");
        }
        if (drawAmount <= 0) {
            throw new IllegalArgumentException("Cannot bet 0 times!");
        }
    }

    //Random valid Form
    public Form(int betAmount, int drawAmount) {
        validateParams(betAmount, drawAmount);
        lotteryNumberSets = new ArrayList<>();
        this.drawAmount = drawAmount;
        for (int i = 0; i < betAmount; i++) {
            lotteryNumberSets.add(new LotteryNumberSet());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Form form) {
            return form.bets().equals(lotteryNumberSets) && form.drawAmount() == drawAmount;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(drawAmount, lotteryNumberSets);
    }
}

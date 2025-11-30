package totolotek.lottery;

import totolotek.finances.Money;
import totolotek.other.NumberHelper;


public class Lottery {
    private final int id;
    private final LotteryNumberSet result;
    private final Money[] rewards;
    private final int[] winners;

    public Lottery(int id, Money[] rewards, LotteryNumberSet numbers, int[] winners) {
        this.id = id;
        this.result = new LotteryNumberSet(numbers);
        this.rewards = rewards;
        this.winners = new int[NumberHelper.REWARD_TYPES];
        System.arraycopy(winners, 0, this.winners, 0, winners.length);
    }

    public int id() {
        return id;
    }

    public LotteryNumberSet result() {
        //gives a deep copy, in order to protect the result from being modified
        return new LotteryNumberSet(result);
    }

    public Money[] rewards() {
        //gives a deep copy, in order to protect the rewards from being modified
        Money[] output = new Money[rewards.length];
        for (int i = 0; i < rewards.length; i++) {
            output[i] = new Money(rewards[i]);
        }
        return output;
    }

    @Override
    public String toString() {
        return "Losowanie nr " + id + "\nWyniki: " + result.toString() + "\n";
    }

    public String detailedToString() {
        StringBuilder builder = new StringBuilder(this.toString());
        boolean noOneWon = true;
        for (int i = 0; i < NumberHelper.REWARD_TYPES; i++) {
            if (winners[i] != 0) {
                noOneWon = false;
                break;
            }
        }
        if (noOneWon) {
            builder.append("Nikt nic nie wygrał w tym losowaniu!\n");
            return builder.toString();
        }
        builder.append("Nagrody:\n");
        for (int i = 0; i < NumberHelper.REWARD_TYPES; i++) {
            if (winners[i] == 0) {
                continue;
            }
            builder.append("Nagrody ");
            builder.append(i + 1);
            builder.append(" stopnia: ");
            builder.append(rewards[i].toString());
            builder.append("\n");
        }
        builder.append("Liczba zwycięskich zakładów:\n");
        for (int i = 0; i < NumberHelper.REWARD_TYPES; i++) {
            if (winners[i] == 0) {
                continue;
            }
            builder.append(i + 1);
            builder.append(" stopnia: ");
            builder.append(winners[i]);
            builder.append("\n");
        }
        builder.append("Łączna pula nagród:\n");
        for (int i = 0; i < NumberHelper.REWARD_TYPES; i++) {
            if (winners[i] == 0) {
                continue;
            }
            builder.append(i + 1);
            builder.append(" stopnia: ");
            builder.append(new Money(0,
                    winners[i] * (rewards[i].groszy() + NumberHelper.GROSZY_PER_ZLOTY * rewards[i].zloty())
            ));
            builder.append("\n");
        }
        return builder.toString();
    }
}

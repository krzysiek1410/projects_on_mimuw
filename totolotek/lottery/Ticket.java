package totolotek.lottery;

import totolotek.finances.Money;
import totolotek.other.NumberHelper;
import totolotek.seller.Headquarters;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Ticket {
    //It is a document, so all the attributes are final
    private final int nrId;
    private final String id;
    private final ArrayList<LotteryNumberSet> lotteryNumberSets;
    private final int lotteriesAmount;
    private final int firstLottery;

    private static final int CANCEL = -1;
    //Zloty:
    private static final long BET_PRICE = 3;
    //Groszy:
    private static final long BET_TAX = 60;

    public Ticket(Form form, int nrId, int collectionPointNumber) {
        this.nrId = nrId;
        lotteryNumberSets = new ArrayList<>(validateBets(new ArrayList<>(form.bets())));
        lotteriesAmount = form.drawAmount();
        firstLottery = Headquarters.getInstance().nextLotteryId();
        //generating unique id
        StringBuilder builder = new StringBuilder();
        int randomNumber = NumberHelper.random().nextInt();
        int controlNumber = 0;
        builder.append(nrId);
        controlNumber += NumberHelper.sumDigits(nrId);
        builder.append('-');
        builder.append(collectionPointNumber);
        controlNumber += NumberHelper.sumDigits(collectionPointNumber);
        builder.append('-');
        builder.append(randomNumber);
        controlNumber += NumberHelper.sumDigits(randomNumber);
        builder.append('-');
        builder.append(NumberHelper.lastTwoDigits(controlNumber));
        id = builder.toString();
    }

    private ArrayList<LotteryNumberSet> validateBets(ArrayList<LotteryNumberSet> lotteryNumberSets) {
        ArrayList<LotteryNumberSet> result = new ArrayList<>();
        for (LotteryNumberSet lotteryNumberSet : lotteryNumberSets) {
            if (lotteryNumberSet.values().size() != NumberHelper.NUMBERS_IN_CORRECT_SET ||
                    lotteryNumberSet.values().contains(CANCEL)) {
                continue;
            }
            result.add(new LotteryNumberSet(lotteryNumberSet));
        }
        return result;
    }

    public int nrId() {
        return nrId;
    }

    public String id() {
        return id;
    }

    public List<LotteryNumberSet> bets() {
        ArrayList<LotteryNumberSet> result = new ArrayList<>();
        for (LotteryNumberSet set : lotteryNumberSets) {
            result.add(new LotteryNumberSet(set));
        }
        return result;
    }

    public int lotteriesAmount() {
        return lotteriesAmount;
    }

    public int firstLottery() {
        return firstLottery;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("KUPON NR ");
        builder.append(id);
        builder.append("\n");
        for (int i = 0; i < lotteryNumberSets.size(); i++) {
            builder.append(i);
            builder.append(": ");
            builder.append(lotteryNumberSets.get(i).toString());
            builder.append("\n");
        }
        builder.append("LICZBA LOSOWAŃ: ");
        builder.append(lotteriesAmount);
        builder.append("\nNUMERY LOSOWAŃ:\n");
        for (int i = firstLottery; i < firstLottery + lotteriesAmount; i++) {
            builder.append(i);
            builder.append(" ");
        }
        builder.append("\nCENA: ");
        builder.append(price().toString());
        builder.append("\n");
        return builder.toString();
    }

    public Money price() {
        return new Money(BET_PRICE * lotteriesAmount * lotteryNumberSets.size());
    }

    public Money tax() {
        return new Money(0, BET_TAX * lotteriesAmount * lotteryNumberSets.size());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Ticket ticket) {
            //id is unique and determines which ticket is it
            return ticket.id.equals(id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nrId, id, lotteryNumberSets, lotteriesAmount, firstLottery);
    }
}

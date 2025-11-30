package totolotek.seller;

import totolotek.exceptions.TransactionErrorException;
import totolotek.exceptions.UnwantedTransactionException;
import totolotek.finances.Money;
import totolotek.lottery.Lottery;
import totolotek.lottery.LotteryNumberSet;
import totolotek.other.CountryBudget;
import totolotek.other.Media;
import totolotek.other.NumberHelper;

import java.util.ArrayList;
import java.util.HashSet;

public class Headquarters {

    private static final int PERCENT_PROFIT = 49;
    private static final int PERCENT_1_REWARD = 44;
    private static final int PERCENT_2_REWARD = 8;
    private static final long ZLOTY_4_REWARD = 24;
    private static final long MINIMUM_1_REWARD = 2000000;
    private static final long MINIMUM_3_REWARD = 36;

    private static final int ID_1_REWARD = 0;
    private static final int ID_2_REWARD = 1;
    private static final int ID_3_REWARD = 2;
    private static final int ID_4_REWARD = 3;
    private static final int REWARD_TYPES = 4;


    private static Headquarters singleton;
    private final Money cumulation;
    private final Money lastLotteryBudget;
    //here is the real money that can be used
    private final Money budget;
    private final ArrayList<Lottery> pastLotteries;
    private int globalTicketsGiven;
    private final ArrayList<CollectionPoint> collectionPoints;

    private Headquarters() {
        cumulation = new Money();
        lastLotteryBudget = new Money();
        budget = new Money();
        pastLotteries = new ArrayList<>();
        collectionPoints = new ArrayList<>();
        globalTicketsGiven = 0;
        HashSet<Integer> fakeLottery = new HashSet<>();
        for (int i = 0; i < 6; i++) {
            fakeLottery.add(NumberHelper.MAX_LOTTERY_NUM + i);
        }
        int[] fakeWinners = new int[4];
        fakeWinners[0] = 0;
        fakeWinners[1] = 0;
        fakeWinners[2] = 0;
        fakeWinners[3] = 0;
        Money[] fakeRewards = new Money[4];
        fakeRewards[0] = new Money();
        fakeRewards[1] = new Money();
        fakeRewards[2] = new Money();
        fakeRewards[3] = new Money();
        pastLotteries.add(new Lottery(0, fakeRewards, new LotteryNumberSet(fakeLottery), fakeWinners));
    }

    public static Headquarters getInstance() {
        if (singleton == null) {
            singleton = new Headquarters();
        }
        return singleton;
    }

    //Why is here this 'unrelated' TransactionErrorException?
    //Well, Players automatically go to take rewards, and then they
    //may generate the TransactionErrorException, so in order to prevent
    //stopping the propagation of the exception, it is simply passed further
    public void lottery() throws TransactionErrorException {
        LotteryNumberSet lotteryNumbers = new LotteryNumberSet();
        //Check how many winners of each type:
        int[] winners = new int[REWARD_TYPES];
        for (CollectionPoint point : collectionPoints) {
            int[] collectionPointWinners = point.howManyWinnersForNumbers(lotteryNumbers);
            for (int i = 0; i < REWARD_TYPES; i++) {
                winners[i] += collectionPointWinners[i];
            }
        }
        //Calculate rewards:
        Money[] rewards = calculateRewards(winners);
        //Create lottery representation and save it:
        Lottery lottery = new Lottery(nextLotteryId(), rewards, lotteryNumbers, winners);
        pastLotteries.add(lottery);
        //Send info to Media:
        Media.getInstance().notifyOfLottery(lottery);
    }

    private Money[] calculateRewards(int[] winners) {
        Money[] rewards = new Money[REWARD_TYPES];
        //profit:
        budget.increase(lastLotteryBudget.percent(PERCENT_PROFIT));
        lastLotteryBudget.decrease(lastLotteryBudget.percent(PERCENT_PROFIT));
        //I tier:
        rewards[ID_1_REWARD] = lastLotteryBudget.percent(PERCENT_1_REWARD);
        if (winners[ID_1_REWARD] != 0) {
            rewards[ID_1_REWARD] = rewards[ID_1_REWARD].floor(winners[ID_1_REWARD]);
        }
        //II tier:
        rewards[ID_2_REWARD] = lastLotteryBudget.percent(PERCENT_2_REWARD);
        if (winners[ID_2_REWARD] != 0) {
            rewards[ID_2_REWARD] = rewards[ID_2_REWARD].floor(winners[ID_2_REWARD]);
        }
        //IV tier:
        rewards[ID_4_REWARD] = new Money(ZLOTY_4_REWARD);
        if (lastLotteryBudget.compareTo(lastLotteryBudget.percent(PERCENT_1_REWARD + PERCENT_2_REWARD).add(new Money(ZLOTY_4_REWARD * winners[ID_4_REWARD]))) < 0) {
            lastLotteryBudget.setToZero();
        } else {
            lastLotteryBudget.decrease(lastLotteryBudget.percent(PERCENT_1_REWARD + PERCENT_2_REWARD).add(new Money(ZLOTY_4_REWARD * winners[ID_4_REWARD])));
        }
        //III tier:
        rewards[ID_3_REWARD] = lastLotteryBudget;
        lastLotteryBudget.setToZero();
        if (winners[ID_3_REWARD] != 0) {
            rewards[ID_3_REWARD] = rewards[ID_3_REWARD].floor(winners[ID_3_REWARD]);
        }
        if (winners[ID_1_REWARD] != 0) {
            rewards[ID_1_REWARD].increase(cumulation);
            cumulation.setToZero();
            if (rewards[ID_1_REWARD].compareTo(new Money(MINIMUM_1_REWARD)) < 0) {
                Money missing = new Money(MINIMUM_1_REWARD).subtract(rewards[ID_1_REWARD]);
                rewards[ID_1_REWARD] = getCash(missing).add(rewards[ID_1_REWARD]);
            }
        } else {
            cumulation.increase(rewards[ID_1_REWARD]);
            rewards[ID_1_REWARD].setToZero();
        }
        if (winners[ID_2_REWARD] == 0) {
            budget.increase(rewards[ID_2_REWARD]);
            rewards[ID_2_REWARD].setToZero();
        }
        if (winners[ID_3_REWARD] == 0) {
            budget.increase(rewards[ID_3_REWARD]);
            rewards[ID_3_REWARD].setToZero();
        } else {
            if (rewards[ID_3_REWARD].compareTo(new Money(MINIMUM_3_REWARD)) < 0) {
                Money missing = new Money(MINIMUM_3_REWARD).subtract(rewards[ID_3_REWARD]);
                rewards[ID_3_REWARD] = getCash(missing).add(rewards[ID_3_REWARD]);
            }
        }
        return rewards;
    }

    private Money getCash(Money amount) {
        if (budget.compareTo(amount) < 0) {
            CountryBudget.getInstance().giveSubsidies(amount.subtract(budget));
            budget.setToZero();
            return new Money(amount);
        }
        budget.decrease(amount);
        return amount;
    }

    private void validateCompanyTransfer(CompanyMoneyTransfer transfer) throws UnwantedTransactionException {
        if (!collectionPoints.contains(transfer.point())) {
            throw new UnwantedTransactionException("Invalid Collection Point!");
        }
    }

    public void receiveMoneyInCompany(CompanyMoneyTransfer transfer) {
        //doesn't check source. All money is good
        budget.increase(transfer.value());
    }

    public Money transferMoneyInCompany(CompanyMoneyTransfer transfer) throws UnwantedTransactionException {
        validateCompanyTransfer(transfer);
        return getCash(transfer.value());
    }

    public CollectionPoint newCollectionPoint() {
        CollectionPoint result = new CollectionPoint();
        collectionPoints.add(result);
        return result;
    }

    public int collectionPointAmount() {
        return collectionPoints.size();
    }

    public CollectionPoint collectionPointWithId(int id) {
        if (id < 0 || id >= collectionPoints.size()) {
            throw new IllegalArgumentException("Collection point with this Id does not exist!");
        }
        return collectionPoints.get(id);
    }

    public int nextLotteryId() {
        return pastLotteries.size();
    }

    public Lottery lotteryWithId(int id) {
        return pastLotteries.get(id);
    }

    public String lotteriesInformation() {
        StringBuilder builder = new StringBuilder("Dotychczasowe losowania:\n");
        for (int i = 1; i < pastLotteries.size(); i++) {
            builder.append(pastLotteries.get(i).detailedToString());
            builder.append('\n');
        }
        return builder.toString();
    }

    public String budgetToString() {
        return "Budżet centrali: " + budget.toString() + "\n";
    }

    public Money budget() {
        return new Money(budget);
    }

    public Lottery lastLottery() {
        return lotteryWithId(pastLotteries.size() - 1);
    }

    //packet visibility is here, so that CollectionPoint (and only it) could use this method
    int giveTicketId() {
        globalTicketsGiven++;
        return globalTicketsGiven;
    }

    @Override
    public String toString() {
        return lotteriesInformation() + budgetToString();
    }
}

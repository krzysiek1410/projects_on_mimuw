package totolotek.seller;

import totolotek.exceptions.InsufficientFundsException;
import totolotek.exceptions.RewardCannotBeGrantedException;
import totolotek.exceptions.TransactionErrorException;
import totolotek.exceptions.UnwantedTransactionException;
import totolotek.finances.Money;
import totolotek.finances.MoneyHolder;
import totolotek.finances.Transaction;
import totolotek.lottery.*;
import totolotek.other.CountryBudget;
import totolotek.other.Media;
import totolotek.other.NumberHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CollectionPoint implements MoneyHolder, LotteryListener {
    private static final int TAX_PERCENT_ON_REWARDS = 10;

    //Boolean represents whether the reward for the ticket was granted
    //When a ticket is given, it is added here as a new key, and value is set to false.
    private final ArrayList<HashMap<Ticket, Boolean>> grantedRewards;
    private final int identifier;
    private final Money budget;

    //packet visibility here, so that only Headquarters could create Collection Points
    CollectionPoint() {
        identifier = Headquarters.getInstance().collectionPointAmount();
        grantedRewards = new ArrayList<>();
        grantedRewards.add(new HashMap<>());
        for (int i = 0; i < Headquarters.getInstance().nextLotteryId(); i++) {
            grantedRewards.add(new HashMap<>());
        }
        budget = new Money();
        Media.getInstance().addSubscriber(this);
    }

    public Ticket sellRandomTicket(int betAmount, int drawAmount,
                                   Transaction transaction) throws TransactionErrorException {
        Form form = new Form(betAmount, drawAmount);
        return sellFormBasedTicket(form, transaction);
    }

    private void saveLotteryTicket(Ticket ticket) {
        grantedRewards.get(Headquarters.getInstance().nextLotteryId()).put(ticket, false);
    }

    public Ticket sellFormBasedTicket(Form form,
                                      Transaction transaction) throws TransactionErrorException {
        Ticket ticket = new Ticket(form, Headquarters.getInstance().giveTicketId(), identifier);
        try {
            transaction.setValue(ticket.price());
            transaction.passMoney();
        } catch (UnwantedTransactionException exception) {
            throw new TransactionErrorException("Transaction denied by payer!");
        } catch (InsufficientFundsException noMoney) {
            //No money? No lottery ticket!
            return null;
        }
        payTaxes(ticket.tax());
        saveLotteryTicket(ticket);
        CompanyMoneyTransfer transfer = newCompanyTransferToHQ(ticket.price().subtract(ticket.tax()));
        transfer.passMoneyToHQ();
        return ticket;
    }

    private int[] calculateReward(Money reward, Money taxes, Ticket ticket, LotteryNumberSet numbers, Money[] rewards) {
        int[] howManyVictories = new int[NumberHelper.REWARD_TYPES];
        if (this.grantedRewards.get(ticket.firstLottery()).get(ticket)) {
            return howManyVictories;
        }
        for (int j = 0; j < ticket.bets().size(); j++) {
            LotteryNumberSet currentNumbers = ticket.bets().get(j);
            int current = currentNumbers.compareSets(numbers);
            if (current < 4) {
                continue;
            }
            if (rewards[6 - current].compareTo(new Money(NumberHelper.MIN_REWARD_FOR_TAX)) >= 0) {
                taxes.increase(rewards[6 - current].percent(TAX_PERCENT_ON_REWARDS));
                reward.increase(new Money(rewards[6 - current].subtract(
                        rewards[6 - current].percent(TAX_PERCENT_ON_REWARDS))
                ));
            } else {
                reward.increase(new Money(rewards[6 - current]));
            }
            howManyVictories[6 - current]++;
        }
        return howManyVictories;
    }

    private int[] calculateReward(Ticket ticket, LotteryNumberSet numbers) {
        Money[] temporary = new Money[NumberHelper.REWARD_TYPES];
        for (int i = 0; i < NumberHelper.REWARD_TYPES; i++) {
            temporary[i] = new Money();
        }
        return calculateReward(new Money(), new Money(), ticket, numbers, temporary);
    }

    public void grantReward(Ticket ticket, Transaction transaction) throws TransactionErrorException {
        //Check if already taken:
        if (!grantedRewards.get(ticket.firstLottery()).containsKey(ticket)) {
            throw new RewardCannotBeGrantedException("Invalid ticket!");
        }
        if (Boolean.TRUE.equals(grantedRewards.get(ticket.firstLottery()).get(ticket))) {
            throw new RewardCannotBeGrantedException("Reward for this ticket was already given!");
        }

        //Calculate reward:
        Money reward = new Money();
        Money taxes = new Money();
        for (int i = 0; i < ticket.lotteriesAmount(); i++) {
            if (ticket.firstLottery() + i >= Headquarters.getInstance().nextLotteryId()) {
                break;
            }
            Lottery currentLottery = Headquarters.getInstance().
                    lotteryWithId(ticket.firstLottery() + i);
            calculateReward(reward, taxes, ticket, currentLottery.result(), currentLottery.rewards());
        }

        //checks if reward is 0:
        if (reward.compareTo(new Money()) == 0) {
            //just set it as given; do nothing else
            grantedRewards.get(ticket.firstLottery()).put(ticket, true);
            return;
        }

        //Getting money for the reward from HQ:
        CompanyMoneyTransfer transfer = newCompanyTransferFromHQ(reward.add(taxes));
        try {
            transfer.passMoneyFromHQ();
        } catch (UnwantedTransactionException e) {
            throw new TransactionErrorException("Internal transaction error in lottery company!");
        }

        //Paying taxes:
        payTaxes(taxes);

        //Sending reward:
        transaction.setValue(reward);
        try {
            transaction.sendMoney(reward);
            budget.decrease(reward);
            transaction.passMoney();
        } catch (InsufficientFundsException e) {
            //this is not even possible!
            throw new TransactionErrorException("Transaction error!");
        } catch (UnwantedTransactionException e) {
            throw new TransactionErrorException("The player denied taking the reward!");
        }
        //Set the ticket as ready:
        grantedRewards.get(ticket.firstLottery()).put(ticket, true);
    }

    private void payTaxes(Money amount) {
        budget.decrease(amount);
        CountryBudget.getInstance().collectTaxes(amount);
    }

    @Override
    public boolean hasMoney(Money amount) {
        //always "has money". If insufficient, then HQ will take subsidies
        return true;
    }

    @Override
    public void receiveMoney(Money amount) {
        budget.increase(amount);
    }

    @Override
    public Money giveMoney(Transaction transaction) throws UnwantedTransactionException {
        throw new UnwantedTransactionException("Collection points don't enter any transactions as a buyer!");
    }

    @Override
    public void notifyOfLottery(Lottery lottery) {
        //extend grantedRewards by a new ID
        grantedRewards.add(new HashMap<>());
        sendAllMoneyToHQ();
    }

    //called from time to time to ensure that the budget of Collection Point is temporary
    private void sendAllMoneyToHQ() {
        CompanyMoneyTransfer transfer = newCompanyTransferToHQ(budget);
        transfer.passMoneyToHQ();
    }

    private CompanyMoneyTransfer newCompanyTransferFromHQ(Money amount) {
        return new CompanyMoneyTransfer(this, amount, true);
    }

    private CompanyMoneyTransfer newCompanyTransferToHQ(Money amount) {
        budget.decrease(amount);
        return new CompanyMoneyTransfer(this, amount, false);
    }

    public int[] howManyWinnersForNumbers(LotteryNumberSet numbers) {
        int[] result = new int[NumberHelper.REWARD_TYPES];
        int size = Headquarters.getInstance().nextLotteryId();
        //have to check previous
        for (int i = Math.max(0, size - NumberHelper.MAX_DRAW_AMOUNT_PER_BET + 1);
             i < size; i++) {

            //Iterate through every ticket:
            for (Map.Entry<Ticket, Boolean> entry : grantedRewards.get(i).entrySet()) {
                Ticket currentTicket = entry.getKey();
                //if out of range
                if (currentTicket.firstLottery() + currentTicket.lotteriesAmount() <= size) {
                    continue;
                }
                int[] currentRewards = calculateReward(currentTicket, numbers);
                for (int j = 0; j < NumberHelper.REWARD_TYPES; j++) {
                    result[j] += currentRewards[j];
                }
            }
        }
        return result;
    }
}

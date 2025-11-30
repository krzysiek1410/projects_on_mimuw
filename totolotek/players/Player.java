package totolotek.players;

import totolotek.exceptions.InsufficientFundsException;
import totolotek.exceptions.TransactionErrorException;
import totolotek.exceptions.UnwantedTransactionException;
import totolotek.finances.Money;
import totolotek.finances.MoneyHolder;
import totolotek.finances.Transaction;
import totolotek.lottery.Form;
import totolotek.lottery.Lottery;
import totolotek.lottery.LotteryListener;
import totolotek.lottery.Ticket;
import totolotek.other.Media;
import totolotek.other.Pair;
import totolotek.seller.CollectionPoint;

import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

public abstract class Player implements MoneyHolder, LotteryListener {
    protected String name;
    protected String surname;
    protected int nrPESEL;
    protected Money money;
    //Every ticket that was ever got is kept here
    protected HashSet<Ticket> tickets;
    protected HashMap<Ticket, CollectionPoint> ticketsCollectionPoint;
    protected PriorityQueue<Pair<Integer, Ticket>> ticketsGetReady;
    //if a player makes a Transaction, then it is the only transaction
    //(sadly, being in two places at the same time is forbidden)
    protected Transaction activeTransaction;

    protected Player(String name, String surname, int nrPESEL) {
        this.name = name;
        this.surname = surname;
        this.nrPESEL = nrPESEL;
        tickets = new HashSet<>();
        ticketsCollectionPoint = new HashMap<>();
        ticketsGetReady = new PriorityQueue<>();
        Media.getInstance().addSubscriber(this);
    }

    protected Player(String name, String surname, int nrPESEL, Money startingMoney) {
        this(name, surname, nrPESEL);
        money = new Money(startingMoney);
    }

    public String name() {
        return name;
    }

    public String surname() {
        return surname;
    }

    public int nrPESEL() {
        return nrPESEL;
    }

    public Money money() {
        return new Money(money);
    }

    //Only unclaimed tickets
    public String[] ticketsIds() {
        if (ticketsGetReady.isEmpty()) {
            return new String[]{"No tickets!"};
        }
        String[] result = new String[ticketsGetReady.size()];
        int i = 0;
        for (Pair<Integer, Ticket> pair : ticketsGetReady) {
            result[i] = pair.second().id();
            i++;
        }
        return result;
    }

    //Includes also claimed tickets (ticket history)
    //though the ticket is physically given away, the player can write down what tickets he had
    public String[] historicalTicketsIds() {
        if (tickets.isEmpty()) {
            return new String[]{"No tickets ever bought!"};
        }
        String[] result = new String[tickets.size()];
        int i = 0;
        for (Ticket ticket : tickets) {
            result[i] = ticket.id();
            i++;
        }
        return result;
    }

    @Override
    public boolean hasMoney(Money amount) {
        return money.compareTo(amount) >= 0;
    }

    @Override
    public void receiveMoney(Money amount) {
        money.increase(amount);

    }

    @Override
    public Money giveMoney(Transaction transaction) throws UnwantedTransactionException, InsufficientFundsException {
        if (transaction != activeTransaction) {
            throw new UnwantedTransactionException("This is not my transaction!");
        }
        if (!hasMoney(transaction.value())) {
            throw new InsufficientFundsException("Insufficient funds!");
        }
        money.decrease(transaction.value());
        return transaction.value();
    }

    protected Ticket buyRandomTicket(CollectionPoint point, int betAmount, int drawAmount) throws TransactionErrorException {
        activeTransaction = new Transaction(this, point);
        return point.sellRandomTicket(betAmount, drawAmount, activeTransaction);
    }

    protected Ticket buyFormTicket(CollectionPoint point, Form form) throws TransactionErrorException {
        activeTransaction = new Transaction(this, point);
        return point.sellFormBasedTicket(form, activeTransaction);
    }

    //it is handled automatically
    protected void getReward(Ticket ticket) throws TransactionErrorException {
        Transaction transaction = new Transaction(ticketsCollectionPoint.get(ticket), this);
        ticketsCollectionPoint.get(ticket).grantReward(ticket, transaction);
        //Forget about the ticket:
        ticketsCollectionPoint.remove(ticket);
        tickets.remove(ticket);
    }

    //works even if ticket is null so it is safe to call buy ticket with no funds
    protected void saveTicket(Ticket ticket, CollectionPoint point) {
        if (ticket == null) {
            return;
        }
        tickets.add(ticket);
        ticketsGetReady.add(new Pair<>(ticket.firstLottery() + ticket.lotteriesAmount() - 1, ticket));
        ticketsCollectionPoint.put(ticket, point);
    }

    //should call saveTicket(ticket), unless it is a feature that the player loses the ticket
    public abstract void buyTicket() throws TransactionErrorException;

    //Standard way of answering to a new Lottery.
    //If there is any ticket, that has now potential reward (even empty) ready to be claimed,
    //then goes to the CollectionPoint and gets the reward
    public void notifyOfLottery(Lottery lottery) throws TransactionErrorException {
        while (!ticketsGetReady.isEmpty() && ticketsGetReady.peek().first() == lottery.id()) {
            //poll() also removes the head of the queue
            //it will not produce NullPointerException; I have checked if not empty!
            getReward(ticketsGetReady.poll().second());
        }
    }

    //called to get rewards for all the tickets
    public void getAllRewards() throws TransactionErrorException {
        while (!ticketsGetReady.isEmpty()) {
            getReward(ticketsGetReady.poll().second());
        }
    }
}

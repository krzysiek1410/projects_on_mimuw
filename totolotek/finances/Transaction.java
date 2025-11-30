package totolotek.finances;

import totolotek.exceptions.InsufficientFundsException;
import totolotek.exceptions.UnwantedTransactionException;

public class Transaction {
    private Money value;
    private final MoneyHolder giver;
    private final MoneyHolder receiver;
    private boolean gotMoney;

    public Transaction(MoneyHolder giver, MoneyHolder receiver) {
        this.giver = giver;
        this.receiver = receiver;
        gotMoney = false;
    }

    public void setValue(Money value) {
        this.value = value;
    }

    public void passMoney() throws UnwantedTransactionException, InsufficientFundsException {
        if (gotMoney) {
            receiver.receiveMoney(value);
            clearTransaction();
            return;
        }
        if (!giver.hasMoney(value)) {
            throw new InsufficientFundsException("Insufficient funds!");
        }
        receiver.receiveMoney(giver.giveMoney(this));
        clearTransaction();
    }

    private void clearTransaction() {
        value = new Money();
        gotMoney = false;
    }

    public void sendMoney(Money amount) throws InsufficientFundsException {
        if (amount.compareTo(value) < 0) {
            throw new InsufficientFundsException("Insufficient funds!");
        }
        gotMoney = true;
        amount.decrease(value);
    }

    public Money value() {
        return new Money(value);
    }
}

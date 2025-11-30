package totolotek.finances;

import totolotek.exceptions.InsufficientFundsException;
import totolotek.exceptions.UnwantedTransactionException;

public interface MoneyHolder {
    boolean hasMoney(Money amount);

    void receiveMoney(Money amount);

    Money giveMoney(Transaction transaction) throws UnwantedTransactionException, InsufficientFundsException;
}

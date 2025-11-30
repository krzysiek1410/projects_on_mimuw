package totolotek.lottery;

import totolotek.exceptions.TransactionErrorException;

public interface LotteryListener {
    void notifyOfLottery(Lottery lottery) throws TransactionErrorException;
}

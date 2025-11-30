package totolotek.other;

import totolotek.exceptions.TransactionErrorException;
import totolotek.lottery.Lottery;
import totolotek.lottery.LotteryListener;

import java.util.ArrayList;

//Message Broker and Singleton
public class Media {
    private static Media singleton;
    private final ArrayList<LotteryListener> subscribers;

    private Media() {
        subscribers = new ArrayList<>();
    }

    public static Media getInstance() {
        if (singleton == null) {
            singleton = new Media();
        }
        return singleton;
    }

    public void notifyOfLottery(Lottery lottery) throws TransactionErrorException {
        for (LotteryListener listener : subscribers) {
            listener.notifyOfLottery(lottery);
        }
    }

    public void addSubscriber(LotteryListener listener) {
        subscribers.add(listener);
    }
}

package totolotek.players;

import totolotek.exceptions.TransactionErrorException;
import totolotek.finances.Money;
import totolotek.lottery.Form;
import totolotek.lottery.Lottery;
import totolotek.seller.CollectionPoint;

import java.util.List;

public class FixedForm extends FavoriteCollectionPointPlayer {

    private Form favoriteForm;
    private int interval;
    private int lotteryCounter;

    public FixedForm(String name, String surname, int nrPESEL, Money startingMoney,
                     List<CollectionPoint> favoriteCollectionPoints, Form favoriteForm,
                     int interval) {
        super(name, surname, nrPESEL, startingMoney, favoriteCollectionPoints);
        initializeAttributes(favoriteForm, interval);
    }

    private void initializeAttributes(Form favoriteForm, int interval) {
        this.favoriteForm = new Form(favoriteForm);
        this.interval = interval;
        lotteryCounter = 0;
    }

    @Override
    public void buyTicket() throws TransactionErrorException {
        CollectionPoint point = nextCollectionPoint();
        saveTicket(buyFormTicket(point, favoriteForm), point);
    }

    @Override
    public void notifyOfLottery(Lottery lottery) throws TransactionErrorException {
        super.notifyOfLottery(lottery);
        lotteryCounter++;
        if (lotteryCounter == interval) {
            lotteryCounter = 0;
            buyTicket();
        }
    }
}

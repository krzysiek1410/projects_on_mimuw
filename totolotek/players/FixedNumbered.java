package totolotek.players;

import totolotek.exceptions.TransactionErrorException;
import totolotek.finances.Money;
import totolotek.lottery.DrawAmountSet;
import totolotek.lottery.Form;
import totolotek.lottery.Lottery;
import totolotek.lottery.LotteryNumberSet;
import totolotek.other.NumberHelper;
import totolotek.seller.CollectionPoint;

import java.util.ArrayList;
import java.util.List;

public class FixedNumbered extends FavoriteCollectionPointPlayer {

    private ArrayList<LotteryNumberSet> favoriteNumbers;

    public FixedNumbered(String name, String surname, int nrPESEL, Money startingMoney,
                         List<CollectionPoint> favoriteCollectionPoints, int[] favoriteNumbers) {
        super(name, surname, nrPESEL, startingMoney, favoriteCollectionPoints);
        initializeFavoriteNumbers(favoriteNumbers);
    }

    private void initializeFavoriteNumbers(int[] favoriteNumbers) {
        if (favoriteNumbers.length != NumberHelper.NUMBERS_IN_CORRECT_SET) {
            throw new IllegalArgumentException("Fixed numbered player has to have exactly " + NumberHelper.NUMBERS_IN_CORRECT_SET + " numbers as favorite numbers!");
        }
        this.favoriteNumbers = new ArrayList<>();
        this.favoriteNumbers.add(new LotteryNumberSet(favoriteNumbers));
    }

    @Override
    public void buyTicket() throws TransactionErrorException {
        Form form = new Form(favoriteNumbers, new DrawAmountSet(new int[]{NumberHelper.MAX_DRAW_AMOUNT_PER_BET}));
        CollectionPoint point = nextCollectionPoint();
        saveTicket(buyFormTicket(point, form), point);
    }

    @Override
    public void notifyOfLottery(Lottery lottery) throws TransactionErrorException {
        super.notifyOfLottery(lottery);
        if (ticketsGetReady.isEmpty()) {
            buyTicket();
        }
    }
}

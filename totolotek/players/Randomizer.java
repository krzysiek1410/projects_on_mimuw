package totolotek.players;

import totolotek.exceptions.TransactionErrorException;
import totolotek.finances.Money;
import totolotek.other.NumberHelper;
import totolotek.seller.CollectionPoint;
import totolotek.seller.Headquarters;

public class Randomizer extends Player {

    private static final int MAX_RANDOMIZER_MONEY = 1000000;
    private static final int MAX_RANDOMIZER_TICKETS_PER_BUYING = 100;

    public Randomizer(String name, String surname, int nrPESEL) {
        super(name, surname, nrPESEL);
        money = new Money(NumberHelper.random().nextInt(MAX_RANDOMIZER_MONEY));
    }

    @Override
    public void buyTicket() throws TransactionErrorException {
        for (int i = 0; i < NumberHelper.random().nextInt(MAX_RANDOMIZER_TICKETS_PER_BUYING) + 1; i++) {
            CollectionPoint point = Headquarters.getInstance().
                    collectionPointWithId(NumberHelper.random().nextInt(
                            Headquarters.getInstance().collectionPointAmount()));

            saveTicket(buyRandomTicket(point, NumberHelper.random().nextInt(NumberHelper.MAX_BETS_PER_FORM) + 1,
                    NumberHelper.random().nextInt(NumberHelper.MAX_DRAW_AMOUNT_PER_BET) + 1), point);
        }
    }
}

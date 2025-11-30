package totolotek.players;

import totolotek.exceptions.TransactionErrorException;
import totolotek.finances.Money;
import totolotek.seller.CollectionPoint;

import java.util.ArrayList;
import java.util.Collections;

public class Minimalist extends FavoriteCollectionPointPlayer {

    public Minimalist(String name, String surname, int nrPESEL, Money startingMoney, CollectionPoint favoriteCollectionPoint) {
        super(name, surname, nrPESEL, startingMoney,
                new ArrayList<>(Collections.singletonList(favoriteCollectionPoint)));
    }


    @Override
    public void buyTicket() throws TransactionErrorException {
        CollectionPoint point = nextCollectionPoint();
        saveTicket(buyRandomTicket(point, 1, 1), point);
    }
}

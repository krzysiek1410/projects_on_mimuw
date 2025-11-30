package totolotek.players;

import totolotek.finances.Money;
import totolotek.seller.CollectionPoint;

import java.util.ArrayList;
import java.util.List;

public abstract class FavoriteCollectionPointPlayer extends Player {
    protected ArrayList<CollectionPoint> favoriteCollectionPoints;
    protected int currentId;

    protected FavoriteCollectionPointPlayer(String name, String surname, int nrPESEL, Money startingMoney,
                                            List<CollectionPoint> favoriteCollectionPoints) {
        super(name, surname, nrPESEL, startingMoney);
        initializeAttributes(favoriteCollectionPoints);
    }

    private void initializeAttributes(List<CollectionPoint> favoriteCollectionPoints) {
        if (favoriteCollectionPoints.isEmpty()) {
            throw new IllegalArgumentException("A player cannot have 0 favorite Collection Points!");
        }
        this.favoriteCollectionPoints = new ArrayList<>(favoriteCollectionPoints);
        currentId = 0;
    }

    protected CollectionPoint nextCollectionPoint() {
        CollectionPoint result = favoriteCollectionPoints.get(currentId);
        currentId++;
        if (currentId == favoriteCollectionPoints.size()) {
            currentId = 0;
        }
        return result;
    }
}

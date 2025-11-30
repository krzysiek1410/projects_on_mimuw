package totolotek.seller;

import totolotek.exceptions.UnwantedTransactionException;
import totolotek.finances.Money;

public class CompanyMoneyTransfer {
    private final CollectionPoint point;
    private final Money value;
    private final boolean isPointGettingMoney;

    public CompanyMoneyTransfer(CollectionPoint point, Money value, boolean isPointGettingMoney) {
        this.value = new Money(value);
        this.point = point;
        this.isPointGettingMoney = isPointGettingMoney;
    }

    public void passMoneyToHQ() {
        if (!isPointGettingMoney) {
            Headquarters.getInstance().receiveMoneyInCompany(this);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public void passMoneyFromHQ() throws UnwantedTransactionException {
        point.receiveMoney(Headquarters.getInstance().transferMoneyInCompany(this));
    }

    public Money value() {
        return new Money(value);
    }

    public CollectionPoint point() {
        return point;
    }
}

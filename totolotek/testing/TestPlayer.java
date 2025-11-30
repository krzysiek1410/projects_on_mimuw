package totolotek.testing;

import totolotek.exceptions.TransactionErrorException;
import totolotek.finances.Money;
import totolotek.lottery.Form;
import totolotek.lottery.Ticket;
import totolotek.players.Player;
import totolotek.seller.CollectionPoint;

import java.util.Set;

public class TestPlayer extends Player {
    public TestPlayer(String name, String surname, int nrPESEL) {
        super(name, surname, nrPESEL, new Money(999999999));
    }

    @Override
    public void buyTicket() {
        //unused
    }

    public void randomTicket(CollectionPoint point, int betAmount, int drawAmount) throws TransactionErrorException {
        super.saveTicket(super.buyRandomTicket(point, betAmount, drawAmount), point);
    }

    public void formTicket(CollectionPoint point, Form form) throws TransactionErrorException {
        super.saveTicket(super.buyFormTicket(point, form), point);
    }

    public Set<Ticket> tickets() {
        return tickets;
    }
}

package totolotek.testing;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import totolotek.finances.Money;
import totolotek.lottery.DrawAmountSet;
import totolotek.lottery.Form;
import totolotek.lottery.LotteryNumberSet;
import totolotek.lottery.Ticket;
import totolotek.other.CountryBudget;
import totolotek.other.NumberHelper;
import totolotek.seller.CollectionPoint;
import totolotek.seller.Headquarters;

import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

public class TotolotekTests {

    private ArrayList<LotteryNumberSet> fullBets;
    private ArrayList<LotteryNumberSet> notFullBets;
    private ArrayList<LotteryNumberSet> someWrongBets;
    private ArrayList<LotteryNumberSet> onlyGoodBets;
    private Form fullBetsForm;
    private Form notFullBetsForm;
    private Form someWrongBetsForm;
    private Ticket fullBetsTicket;
    private Ticket notFullBetsTicket;
    private Ticket someWrongBetsTicket;
    private static CollectionPoint point1;
    private static CollectionPoint point2;
    private static CollectionPoint point3;
    private TestPlayer testPlayer;


    @BeforeAll
    public static void prepTests() {
        point1 = Headquarters.getInstance().newCollectionPoint();
        point2 = Headquarters.getInstance().newCollectionPoint();
        point3 = Headquarters.getInstance().newCollectionPoint();
    }

    @BeforeEach
    public void prepVars() {
        testPlayer = new TestPlayer("", "", 0);
        fullBets = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            fullBets.add(generateNumberSet(6, false));
        }
        notFullBets = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            notFullBets.add(generateNumberSet(6, false));
        }
        someWrongBets = new ArrayList<>();
        onlyGoodBets = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            if (i == 2) {
                someWrongBets.add(generateNumberSet(5, true));
            } else if (i == 4) {
                someWrongBets.add(generateNumberSet(4, false));
            } else if (i == 0) {
                someWrongBets.add(generateNumberSet(10, false));
            } else {
                LotteryNumberSet temporary = generateNumberSet(6, false);
                onlyGoodBets.add(temporary);
                someWrongBets.add(temporary);
            }
        }
        fullBetsForm = new Form(fullBets, new DrawAmountSet(new int[]{3}));
        notFullBetsForm = new Form(notFullBets, new DrawAmountSet(new int[]{1}));
        someWrongBetsForm = new Form(someWrongBets, new DrawAmountSet(new int[]{10}));
        fullBetsTicket = new Ticket(fullBetsForm, 1, 1);
        notFullBetsTicket = new Ticket(notFullBetsForm, 2, 2);
        someWrongBetsTicket = new Ticket(someWrongBetsForm, 3, 3);
    }

    private Form generateForm(int betAmount) {
        ArrayList<LotteryNumberSet> lotteryNumberSets = new ArrayList<>();
        for (int i = 0; i < betAmount; i++) {
            lotteryNumberSets.add(new LotteryNumberSet());
        }
        return new Form(lotteryNumberSets, new DrawAmountSet(new int[]{betAmount}));
    }

    private LotteryNumberSet generateNumberSet(int numberAmount, boolean cancel) {
        HashSet<Integer> numbers = new HashSet<>();
        if (cancel) {
            numbers.add(-1);
        }
        for (int i = 0; i < numberAmount; i++) {
            int next = NumberHelper.random().nextInt(NumberHelper.MAX_LOTTERY_NUM) + 1;
            while (numbers.contains(next)) {
                next = NumberHelper.random().nextInt(NumberHelper.MAX_LOTTERY_NUM) + 1;
            }
            numbers.add(next);
        }
        return new LotteryNumberSet(numbers);
    }

    @Test
    public void testFormExceptions() {
        assertThrows(IllegalArgumentException.class, () -> generateForm(NumberHelper.MAX_BETS_PER_FORM + 1));
        assertThrows(IllegalArgumentException.class, () -> generateForm(0));
        for (int i = 0; i < NumberHelper.MAX_BETS_PER_FORM; i++) {
            int copyI = i + 1;
            assertDoesNotThrow(() -> generateForm(copyI));
        }
    }

    @Test
    public void testFormAttributes() {
        assertEquals(fullBetsForm.bets(), fullBets);
        assertEquals(notFullBetsForm.bets(), notFullBets);
        assertEquals(someWrongBetsForm.bets(), someWrongBets);
        assertEquals(3, fullBetsForm.drawAmount());
        assertEquals(1, notFullBetsForm.drawAmount());
        assertEquals(10, someWrongBetsForm.drawAmount());
    }

    @Test
    public void testTicketAttributes() {
        assertEquals(3, fullBetsTicket.lotteriesAmount());
        assertEquals(1, notFullBetsTicket.lotteriesAmount());
        assertEquals(10, someWrongBetsTicket.lotteriesAmount());
        assertEquals(fullBetsTicket.bets(), fullBets);
        assertEquals(notFullBetsTicket.bets(), notFullBets);
        assertEquals(someWrongBetsTicket.bets(), onlyGoodBets);
        assertEquals(1, fullBetsTicket.firstLottery());
        assertEquals(1, notFullBetsTicket.firstLottery());
        assertEquals(1, someWrongBetsTicket.firstLottery());
        assertEquals(1, fullBetsTicket.nrId());
        assertEquals(2, notFullBetsTicket.nrId());
        assertEquals(3, someWrongBetsTicket.nrId());
        assertEquals(new Money(8L * 3L * 3L), fullBetsTicket.price());
        assertEquals(new Money(0, 24L * 60L), fullBetsTicket.tax());
        assertEquals(new Money(4L * 3L), notFullBetsTicket.price());
        assertEquals(new Money(0, 4L * 60L), notFullBetsTicket.tax());
        assertEquals(new Money(10L * 3L * 3L), someWrongBetsTicket.price());
        assertEquals(new Money(0, 10L * 3L * 60L), someWrongBetsTicket.tax());
    }

    private void testBuyingSingleBet(Form referenceForm, Ticket referenceTicket, CollectionPoint point) {
        Money taxBefore = CountryBudget.getInstance().taxCollected();
        Money budgetBefore = Headquarters.getInstance().budget();
        assertDoesNotThrow(() -> testPlayer.formTicket(point, referenceForm));
        Ticket shadowTicket = testPlayer.tickets().iterator().next();
        assertEquals(shadowTicket.lotteriesAmount(), referenceTicket.lotteriesAmount());
        assertEquals(shadowTicket.firstLottery(), referenceTicket.firstLottery());
        assertEquals(shadowTicket.bets(), referenceTicket.bets());
        assertEquals(shadowTicket.price(), referenceTicket.price());
        assertEquals(shadowTicket.tax(), referenceTicket.tax());
        assertEquals(new Money(999999999).subtract(shadowTicket.price()), testPlayer.money());
        assertEquals(new Money(shadowTicket.tax()), CountryBudget.getInstance().taxCollected().subtract(taxBefore));
        assertEquals(new Money(shadowTicket.price()).subtract(shadowTicket.tax()), Headquarters.getInstance().budget().subtract(budgetBefore));
    }

    @Test
    public void testBuyingTicketFullBet() {
        testBuyingSingleBet(fullBetsForm, fullBetsTicket, point1);
    }

    @Test
    public void testBuyingTicketNotFullBet() {
        testBuyingSingleBet(notFullBetsForm, notFullBetsTicket, point2);
    }

    @Test
    public void testBuyingSingleWrongNumbersBet() {
        testBuyingSingleBet(someWrongBetsForm, someWrongBetsTicket, point3);
    }

    @Test
    public void testBuyingRandomTicket() {
        for (int i = 0; i < 10; i++) {
            assertDoesNotThrow(() -> testPlayer.randomTicket(point1, 1, 1));
        }
        assertEquals(10, testPlayer.tickets().size());
        for (Ticket ticket : testPlayer.tickets()) {
            assertEquals(6, ticket.bets().iterator().next().values().size());
            assertEquals(1, ticket.firstLottery());
        }
    }
}

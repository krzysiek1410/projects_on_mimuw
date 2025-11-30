package totolotek.testing;

import totolotek.exceptions.TransactionErrorException;
import totolotek.finances.Money;
import totolotek.lottery.Form;
import totolotek.other.CountryBudget;
import totolotek.other.NumberHelper;
import totolotek.players.*;
import totolotek.seller.CollectionPoint;
import totolotek.seller.Headquarters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class Presentation {

    private static ArrayList<String> names;
    private static ArrayList<String> surnames;

    private static void fill() {
        names = new ArrayList<>();
        surnames = new ArrayList<>();
        //Przykładowe imiona
        names.add("Jan");
        names.add("Piotr");
        names.add("Siemowit");
        names.add("Michał");
        names.add("Karol");
        names.add("Wacław");
        names.add("Krzysztof");
        names.add("Ignacy");
        names.add("Jakub");
        names.add("Antoni");
        names.add("Franciszek");
        names.add("Paweł");
        //15 najpopularniejszych nazwisk w Polsce:
        surnames.add("Nowak");
        surnames.add("Kowalski");
        surnames.add("Wiśniewski");
        surnames.add("Wójcik");
        surnames.add("Kowalczyk");
        surnames.add("Kamiński");
        surnames.add("Lewandowski");
        surnames.add("Zieliński");
        surnames.add("Szymański");
        surnames.add("Woźniak");
        surnames.add("Dąbrowski");
        surnames.add("Kozłowski");
        surnames.add("Mazur");
        surnames.add("Jankowski");
        surnames.add("Kwiatkowski");
    }

    private static String[] randomNameAndSurnameAndPESEL() {
        String[] result = new String[3];
        result[0] = names.get(NumberHelper.random().nextInt(names.size()));
        result[1] = surnames.get(NumberHelper.random().nextInt(surnames.size()));
        result[2] = Integer.toString(NumberHelper.random().nextInt(899999999) + 100000000);
        return result;
    }

    private static int nextID(int integer) {
        integer++;
        if (integer == 10) {
            integer = 0;
        }
        return integer;
    }

    public static void functionalitiesPresentation() throws TransactionErrorException {
        fill();
        CollectionPoint[] points = new CollectionPoint[10];
        for (int i = 0; i < 10; i++) {
            points[i] = Headquarters.getInstance().newCollectionPoint();
        }
        Player[] players = new Player[800];
        for (int i = 0; i < 200; i++) {
            String[] random = randomNameAndSurnameAndPESEL();
            players[i] = new Randomizer(random[0], random[1], Integer.parseInt(random[2]));
        }
        int collectionPointId = 0;
        for (int i = 200; i < 400; i++) {
            String[] random = randomNameAndSurnameAndPESEL();
            players[i] = new Minimalist(random[0], random[1], Integer.parseInt(random[2]),
                    new Money(NumberHelper.random().nextInt(1000)), points[collectionPointId]);
            collectionPointId = nextID(collectionPointId);
        }
        for (int i = 400; i < 600; i++) {
            String[] random = randomNameAndSurnameAndPESEL();
            int interval = NumberHelper.random().nextInt(5);
            Form favoriteForm = new Form(NumberHelper.random().nextInt(NumberHelper.MAX_BETS_PER_FORM) + 1,
                    NumberHelper.random().nextInt(NumberHelper.MAX_DRAW_AMOUNT_PER_BET) + 1);
            players[i] = new FixedForm(random[0], random[1], Integer.parseInt(random[2]),
                    new Money(NumberHelper.random().nextInt(1000)),
                    new ArrayList<>(Collections.singletonList(points[collectionPointId])), favoriteForm, interval);
            collectionPointId = nextID(collectionPointId);
        }
        for (int i = 600; i < 800; i++) {
            String[] random = randomNameAndSurnameAndPESEL();
            int[] numbers = new int[NumberHelper.NUMBERS_IN_CORRECT_SET];
            HashSet<Integer> temporary = new HashSet<>();
            while (temporary.size() < NumberHelper.NUMBERS_IN_CORRECT_SET) {
                temporary.add(NumberHelper.random().nextInt(NumberHelper.MAX_LOTTERY_NUM) + 1);
            }
            int id = 0;
            for (int integer : temporary) {
                numbers[id] = integer;
                id++;
            }
            players[i] = new FixedNumbered(random[0], random[1], Integer.parseInt(random[2]),
                    new Money(NumberHelper.random().nextInt(1000)),
                    new ArrayList<>(Collections.singletonList(points[collectionPointId])), numbers);
            collectionPointId = nextID(collectionPointId);
        }
        for (int i = 0; i < 20; i++) {
            for (Player player : players) {
                player.buyTicket();
            }
            Headquarters.getInstance().lottery();
        }
        for (Player player : players) {
            player.getAllRewards();
        }
        System.out.printf(Headquarters.getInstance().toString());
        String out = CountryBudget.getInstance().toString();
        System.out.printf(out);

    }

    private Presentation() {
    }
}

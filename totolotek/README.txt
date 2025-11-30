Kilka dodatkowych informacji na temat tego projektu:
 -> Użytkownicy automatycznie odbierają nagrody, w momencie, gdy wszystkie losowania,
    których dotyczył dany kupon, się już odbędą. Dodałem również metodę dla gracza
    getAllRewards, która sprawia, że gracz odbiera nagrody za wszystkie posiadane kupony,
    niezależnie czy wszystkie losowania, których dotyczy dany kupon, się już odbyły.
    Mogłoby to odzwierciedlać na przykład sytuację, gdzie gracz się przeprowadza i wie,
    że niebawem nie będzie mógł zgłosić się do kolektur z rodzinnego miasta, więc odbiera
    na raz wszystkie możliwe nagrody. Ta metoda jest dostępna dla użytkownika klasy.
 -> Klasa Money, w zależności od kontekstu, reprezentuje zarówno faktyczne pieniądze,
    które dany obiekt posiada, jak również kwotę w rozumieniu abstrakcyjnym, np. cenę.
 -> Klasy Transaction i CompanyMoneyTransfer odpowiadają za fakt, żeby tylko uprawnieni
    mogli poprosić klasę o pieniądze, żeby nie mogła przyjść klasa z zewnątrz i
    poprosić np. gracza o wszystkie pieniądze, jakie posiada, a jedynie, żeby płacił
    za to, za co faktycznie planował zapłacić (to gracz wychodzi z inicjatywą transakcji).
    Te klasy koordynują jedynie przepływ pieniędzy; nie są odpowiedzialne za przepływ produktów.
    Jest to chyba wzorzec projektowy mediator. W obecnym stanie ani kolektura, ani
    Centrala nigdy nie dostają wskaźnika na gracza. Warto dodać, że przepływ pieniędzy między budżetem
    państwa a jakimkolwiek obiektem nie odbywa się poprzez transakcję (gdyby rozwijać projekt, to należałoby
    tutaj wykonać specjalny rodzaj transakcji, gdzie centrala składałaby odpowiednie papiery
    potrzebne do uzyskania subwencji, a dopiero potem państwo inicjowałoby i realizowałoby transakcję
    po rozpatrzeniu wniosku, ale tego tu nie modelujemy).
 -> Wielokrotnie będzie wywoływane buyTicket() dla gracza, który nie ma już dosyć pieniędzy.
    Nie jest to błąd. Jest to feature, korzystny na przykład przy graczu losowym, który może
    nie mieć pieniędzy na kupno kuponu, a przy innych wylosowanych liczbach już mieć, więc
    nie chcemy, żeby po pierwszym nieudanym zakupie kończył on kupować kupony. Wywołanie
    buyTicket(), gdy gracz nie ma środków, powinno zakończyć się po prostu tym samym,
    czym brak działania, a konkretniej:
        Gracz poprosi o kupon, ale nie zapłaci, więc dostanie null. W sytuacji standardowej
        spróbuje go zapisać, ale to nie zrobi absolutnie nic.
 -> W przypadku mediów zrobiłem Singleton, gdyż nie modeluję w tym zadaniu różnych rodzajów
    mediów, a więc reprezentuje ta klasa szeroko pojęte media, a więc pojedynczy obiekt
    reprezentujący pojęcie abstrakcyjne (możnaby oczywiście rozwinąć projekt, na przykład
    o różne środki przekazu i potem dodać gracza, który tylko gazety czyta i jeżeli w gazecie
    nie będzie informacji o kolejnym losowaniu, a te na przykłąd mogłyby być dopiero, jak ktoś
    wytypuje szóstkę, to taki gracz do tego czasu nie odbierze swoich nagród. Moje rozwiązanie
    pozwalałoby to dopisać, choć pewnie wtedy należałoby zrezygnować z owego singletona).
    Ponadto tak napisane media odgrywają rolę Brokera we wzorcu projektowym Broker.
 -> Kolektury należy tworzyć w oparciu, o metodę dostarczaną przez centralę.
    Reprezentuje to rzeczywiste działanie, że to centrala tworzy nowe placówki. W przeciwieństwie
    do podejścia gdzie, gdy placówka się tworzy, to następnie zgłasza się sama do centrali,
    by poinformować ją o swoim istnieniu (takie podejście by można robić,
    gdyby chcieć odwzorować franczyzy lub tego typu rzeczy).
 -> Kolektura posiada własne środki tymczasowe, jedynie w celu finalizowania obecnych zadań
    (kupno kuponu lub wydanie nagrody). Pozwala to na przykład na zgrabniejsze zarządzanie środkami
    potrzebnymi na sfinalizowanie zakupu kuponu przez gracza, zapobiega to np. sytuacji, że kolektura
    wysyła od razu pieniądze otrzymane od gracza do centrali (nie mając własnego budżetu) i zaraz potem
    o nie prosi, żeby zapłacić podatek za ten zakup. (Gdyby to była rzeczywista sytuacja, to
    centrala szybko by się przeciążyła, gdyby musiała płacić podatek od razu po każdym zakupie
    lub gdyby było tyle transakcji z kolekturami jak w opisanym powyżej przykładzie)
    Ten budżet jest wysyłany do centrali po każdej poprawnie zakończonej operacji  oraz
    po każdym losowaniu, żeby w razie błędów skutkujących zostawieniem pieniędzy w budżecie
    kolektur, co pewien czas korygować go.
 -> W wielu miejscach, w przypadku akcesorów zwracających atrybuty obiektu, zwracam
    głęboką kopię atrybutu, w celu ochrony, żeby użytkownik klasy nie mógł zmienić
    atrybutu poprzez zmianę tego, co dostaje jako wynik takiego akcesora.
 -> Przyjąłem założenie, że w przypadku otrzymania nulli jako argumentów metod, klasy,
    które są implementacją tego zadania, mają nieokreślone zachowanie i użytkownik nie
    powinien nic o nim zakładać (wystąpi najczęściej NullPointerException, ale to dobrze,
    bo to pokaże użytkownikowi, że błędnie korzysta z dostarczonych przeze mnie klas).

W rozwiązaniu przyjąłem pewne tłumaczenia słów z polskiej wersji (nie zawsze 1 do 1 co do słowa):
 -> Centrala - Headquarters
 -> Losowanie - Lottery
 -> Blankiet - Form
 -> Kolektura - Collection Point
 -> Kupon - Ticket
 -> Gracz - Player
 -> Minimalista - Minimalist
 -> Gracz losowy - Randomizer
 -> Gracz stałoliczbowy - FixedNumbered
 -> Gracz stałoblankietowy - FixedForm
 -> Budżet państwa - CountryBudget
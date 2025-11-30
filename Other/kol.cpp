#include <cstdlib>

#include "kol.h"

using namespace std;


//Niektóre fragmenty kodu zostały wzięte ze skryptu na Moodle
namespace {

    //kolejka dwustronna z operacją odwracania (bez ustalonego kierunku)
    typedef struct lista {
        interesant *head;
        interesant *tail;
    } lista;

    //standardowa operacja na listach
    lista create_list() {
        lista res;
        res.head = (interesant *) malloc(sizeof(interesant));
        res.tail = (interesant *) malloc(sizeof(interesant));
        res.head->l1 = NULL;
        res.head->l2 = res.tail;
        res.tail->l1 = NULL;
        res.tail->l2 = res.head;
        return res;
    }

    //standardowa operacja na listach
    bool isempty(const lista l) {
        return l.head->l2 == l.tail;
    }

    //standardowa operacja na listach
    interesant *front(const lista l) {
        return l.head->l2;
    }

    //standardowa operacja na listach
    interesant *back(const lista l) {
        return l.tail->l2;
    }

    //standardowa operacja na listach
    void link(interesant *el, interesant const *old, interesant *nw) {
        if (el) {
            if (el->l1 == old) {
                el->l1 = nw;
            } else {
                el->l2 = nw;
            }
        }
    }

    //standardowa operacja na listach
    void push_back(lista &l, int x) {
        interesant *el = (interesant *) malloc(sizeof(interesant));
        el->val = x;
        el->l1 = l.tail;
        el->l2 = l.tail->l2;
        link(l.tail->l2, l.tail, el);
        l.tail->l2 = el;
    }

    //standardowa operacja na listach
    void remove_interesant(interesant *el) {
        link(el->l1, el, el->l2);
        link(el->l2, el, el->l1);
    }

    //standardowa operacja na listach
    void pop_front(lista &l) {
        if (!isempty(l)) {
            remove_interesant(l.head->l2);
        }
    }

    //standardowa operacja na listach
    void reverse(lista &l) {
        interesant *tmp = l.head;
        l.head = l.tail;
        l.tail = tmp;
    }

    //standardowa operacja na listach
    void append(lista &l1, lista &l2) {
        interesant *b = l1.tail;
        interesant *f = l2.head;
        link(b->l2, b, f->l2);
        link(f->l2, f, b->l2);
        b->l2 = f;
        f->l2 = b;
        l1.tail = l2.tail;
        l2.head = f;
        l2.tail = b;
    }

    vector<lista> kolejki;

    int akt_numerek;

}

void otwarcie_urzedu(int m) {
    kolejki.resize(m);
    for (int i = 0; i < m; ++i)
        kolejki[i] = create_list();
    akt_numerek = 0;
}

interesant *nowy_interesant(int k) {
    push_back(kolejki[k], akt_numerek++);
    return back(kolejki[k]);
}

int numerek(interesant *i) {
    return i->val;
}

interesant *obsluz(int k) {
    if (isempty(kolejki[k]))
        return NULL;
    interesant *i = front(kolejki[k]);
    pop_front(kolejki[k]);
    return i;
}

void zmiana_okienka(interesant *i, int k) {
    remove_interesant(i);
    i->l1 = kolejki[k].tail;
    i->l2 = kolejki[k].tail->l2;
    link(kolejki[k].tail->l2, kolejki[k].tail, i);
    kolejki[k].tail->l2 = i;
}

void zamkniecie_okienka(int k1, int k2) {
    append(kolejki[k2], kolejki[k1]);
}

std::vector<interesant *> fast_track(interesant *i1, interesant *i2) {

    if (i1 == i2) {
        remove_interesant(i1);
        vector<interesant*> wyn;
        wyn.push_back(i1);
        return wyn;
    }

    //znalezc strone w ktora nalezy isc
    interesant *i1L = i1->l2;
    interesant *i1L_prev = i1;
    interesant *i1P = i1->l1;
    interesant *i1P_prev = i1;
    while (i1L != i2 && i1P != i2) {
        if ((i1L->l1 == NULL || i1L->l2 == NULL) && (i1P->l1 == NULL || i1P->l2 == NULL)) {
            break;
        }
        if (i1L->l1 != NULL && i1L->l2 != NULL) {
            if (i1L->l1 == i1L_prev) {
                i1L_prev = i1L;
                i1L = i1L->l2;
            } else {
                i1L_prev = i1L;
                i1L = i1L->l1;
            }
        }
        if (i1P->l1 != NULL && i1P->l2 != NULL) {
            if (i1P->l1 == i1P_prev) {
                i1P_prev = i1P;
                i1P = i1P->l2;
            } else {
                i1P_prev = i1P;
                i1P = i1P->l1;
            }
        }
    }
    interesant *prev = i1;
    vector<interesant *> wynik;
    interesant *akt;
    if (i1L == i2) {
        akt = i1->l2;
    } else {
        akt = i1->l1;
    }
    wynik.push_back(i1);
    wynik.push_back(akt);
    while (akt != i2) {
        if (akt == NULL) break;
        if (akt->l2 != NULL && akt->l2 == prev) {
            remove_interesant(prev);
            prev = akt;
            akt = akt->l1;
        } else if (akt->l1 != NULL && akt->l1 == prev){
            remove_interesant(prev);
            prev = akt;
            akt = akt->l2;
        }
        wynik.push_back(akt);
    }
    remove_interesant(prev);
    remove_interesant(akt);
    return wynik;
}

void naczelnik(int k) {
    reverse(kolejki[k]);
}

vector<interesant *> zamkniecie_urzedu() {
    vector<interesant *> wynik;
    for (auto i : kolejki) {
        while (i.tail->l2 != i.head) {
            wynik.push_back(i.head->l2);
            remove_interesant(i.head->l2);
        }
        free(i.head);
        free(i.tail);
    }
    kolejki.clear();
    return wynik;
}
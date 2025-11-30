#include <iostream>
#include <vector>
#include <queue>
#include <map>
#include <numeric>

using namespace std;

class Stan {
public:
    vector<int> szkl;
    bool operator==(const Stan& comp) const {
        for (int i = 0; i < (int)(comp.szkl.size()); i++) {
            if (comp.szkl[i] != this->szkl[i]) return false;
        }
        return true;
    }
    bool operator<(const Stan& comp) const {
        for (int i = 0; i < (int)(comp.szkl.size()); i++) {
            if (comp.szkl[i] > this->szkl[i]) return true;
            if (comp.szkl[i] < this->szkl[i]) return false;
        }
        return false;
    }
    void operator=(const vector<int>& nowyStan) {
        szkl = nowyStan;
    }

};


void symulujRuchy(map<Stan, int>& znalezione, const Stan& cel, Stan startowy, const vector<int>& limity) {
    queue<pair<int, Stan>> stany;
    stany.emplace(-1, startowy);
    while(!stany.empty()) {
        Stan obecny = stany.front().second;
        int liczba_ruchow = stany.front().first;
        stany.pop();
        if(znalezione[obecny] >= liczba_ruchow && znalezione[obecny] != 0) {
            continue;
        }
        znalezione[obecny] = liczba_ruchow;
        if (cel == obecny) {
            break;
        }
        Stan kolejny = obecny;
        //wlewanie do pelna
        for (int i = 0; i < (int)(obecny.szkl.size()); i++) {
            kolejny.szkl[i] = limity[i];
            if (znalezione[kolejny] < liczba_ruchow - 1 || znalezione[kolejny] == 0) {
                stany.emplace(liczba_ruchow - 1, kolejny);
            }
            kolejny.szkl[i] = obecny.szkl[i];
        }
        //wylewanie do 0
        for (int i = 0; i < (int)obecny.szkl.size(); i++) {
            kolejny.szkl[i] = 0;
            if (znalezione[kolejny] < liczba_ruchow - 1 || znalezione[kolejny] == 0) {
                stany.emplace(liczba_ruchow - 1, kolejny);
            }
            kolejny.szkl[i] = obecny.szkl[i];
        }
        //przelewanie z i do j ile sie da
        for (int i = 0; i < (int)(obecny.szkl.size()); i++) {
            for (int j = 0; j < (int)(obecny.szkl.size()); j++) {
                kolejny.szkl[j] = min(limity[j], kolejny.szkl[j] + kolejny.szkl[i]);
                kolejny.szkl[i] = kolejny.szkl[i] - kolejny.szkl[j] + obecny.szkl[j];
                if (znalezione[kolejny] < liczba_ruchow - 1 || znalezione[kolejny] == 0) {
                    stany.emplace(liczba_ruchow - 1, kolejny);
                }
                kolejny.szkl[j] = obecny.szkl[j];
                kolejny.szkl[i] = obecny.szkl[i];
            }
        }
    }

}


int przelewaneczka(const Stan& cel, const vector<int>& limity, const int& n) {
    if (n == 0) return -1;
    map<Stan, int> znalezione;
    Stan start;
    for (int i = 0; i < n; i++) {
        start.szkl.push_back(0);
    }
    //sprawdzam dwa przypadki gdy jest pewnosc ze nie da sie tak rozlac
    bool czyZepsute = true;
    for (int i = 0; i < n; i++) {
        if (cel.szkl[i] == 0 || cel.szkl[i] == limity[i]) {
            czyZepsute = false;
            break;
        }
    }
    if (czyZepsute) return 0;
    int caloscNWD = limity[0];
    for (int i = 1; i < n; i++) {
        caloscNWD = gcd(caloscNWD, limity[i]);
    }
    if (caloscNWD != 1) {
        for (int i = 0; i < n; i++) {
            if (gcd(caloscNWD, cel.szkl[i]) != caloscNWD) {
                czyZepsute = true;
                break;
            }
        }
    }
    if (czyZepsute) return 0;
    symulujRuchy(znalezione, cel, start, limity);
    return znalezione[cel];
}

int main() {
    int n2; //liczba szklanek bez zerowych
    int n;
    cin >> n;
    n2 = n;
    vector<int> limity;
    Stan cel;
    for (int i = 0; i < n; i++) {
        int x;
        int y;
        cin >> x >> y;
        if(x == 0) {
            n2--;
            continue;
        }
        limity.push_back(x);
        cel.szkl.push_back(y);
    }
    int wynik = (-1)*(przelewaneczka(cel, limity, n2) + 1);
    cout << wynik;
}
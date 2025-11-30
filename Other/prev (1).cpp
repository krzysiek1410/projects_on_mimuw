#include "prev.h"
#include <vector>
#include <set>
#include <climits>
#include <memory>
#include <cmath>
#include <algorithm>

using namespace std;

class PersistentSegmentTree {
public:
    void pushBack(int value);

    int prevInRange(int index, int begin, int end) const;

    void done();

private:
    struct Vertex {
        struct Range {
            int begin;
            int end;
        };

        Vertex* leftChild = nullptr;
        Vertex* rightChild = nullptr;
        Range range;
        int hIdx;
        int height = -1;

        Vertex(int val, int hIdVal) {
            leftChild = nullptr;
            rightChild = nullptr;
            range.begin = val;
            range.end = val;
            hIdx = hIdVal;
            height = 0;
        }

        Vertex(Vertex* l, Vertex* r) {
            leftChild = l;
            rightChild = r;
            if (l) {
                range.begin = l->range.begin;
                range.end = l->range.end;
                hIdx = leftChild->hIdx;
                height = leftChild->height + 1;
            }
            if (r) {
                range.end = r->range.end;
                hIdx = max(leftChild->hIdx, rightChild->hIdx);
                height = max(leftChild->height, rightChild->height) + 1;
            }
        }

        bool isLeaf() {
            return (range.begin == range.end);
        }
    };

    vector<Vertex*> roots;
    vector<unique_ptr<Vertex>> vertices;

    //-1 - po lewo
    //0 - dobrze
    //1 - po prawo
    int getLPR(int value, Vertex* vertex) const {
        int result = -1;
        if (vertex->isLeaf()) {
            if (value == vertex->range.begin) {
                result = 0;
            }
            if (value > vertex->range.begin) {
                result = 1;
            }
        } else if (value > vertex->leftChild->range.end) {
            result = 1;
        }
        return result;
    }

    vector<Vertex*> path(int value, Vertex* root) const {
        Vertex* vertex = root;
        vector<Vertex*> resultPath;
        for (int i = 0; i < INT_MAX; i++) { //<=> while(true) ale zatrzyma sie gdyby cos sie zepsulo
            resultPath.push_back(vertex);
            if (vertex->isLeaf()) break;
            if (getLPR(value, vertex) == -1) {
                vertex = vertex->leftChild;
            } else {
                vertex = vertex->rightChild;
            }
        }
        return resultPath;
    }

    Vertex* addVertex(Vertex* v) {
        vertices.emplace_back(v);
        return vertices.back().get();
    }

    Vertex* update(int value, vector<Vertex*> &newPath, int idx, Vertex* leaf) {
        Vertex *resultVertex;
        if (leaf == nullptr) return nullptr;
        int isLPR = getLPR(value, leaf);
        if (!isLPR) {
            resultVertex = addVertex(new Vertex(*leaf));
            resultVertex->hIdx = idx;
        } else {
            Vertex* leafVertex = addVertex(new Vertex(*leaf));
            Vertex* newLeafVertex = addVertex(new Vertex(value, idx));
            if (isLPR == -1) {
                resultVertex = addVertex(new Vertex(newLeafVertex, leafVertex));
            } else {
                resultVertex = addVertex(new Vertex(leafVertex, newLeafVertex));
            }
        }
        for (int i = (int) newPath.size() - 1; i >= 0; i--) {
            Vertex* vertex = newPath[(size_t) i];
            Vertex* curr = addVertex(new Vertex(*vertex));
            isLPR = getLPR(value, vertex);
            if (!isLPR || isLPR == 1) {
                *curr = Vertex(curr->leftChild, resultVertex);
            } else {
                *curr = Vertex(resultVertex, curr->rightChild);
            }
            if (abs(curr->leftChild->height - curr->rightChild->height) > 1) {
                if (getLPR(value, resultVertex) == -1) {
                    recreate(curr, resultVertex, resultVertex->leftChild);
                } else {
                    recreate(curr, resultVertex, resultVertex->rightChild);
                }
            }
            resultVertex = curr;
        }
        return resultVertex;
    }

    void recreate(Vertex* curr, Vertex* prev, Vertex* next) {
        Vertex* v1 = (curr->leftChild == prev) ? curr->rightChild : curr->leftChild;
        Vertex* v2 = (prev->leftChild == next) ? prev->rightChild : prev->leftChild;
        Vertex* v3 = next->leftChild;
        Vertex* v4 = next->rightChild;
        vector<Vertex*> toExtend = {v1, v2, v3, v4};
        sort(toExtend.begin(), toExtend.end(), [](Vertex* a, Vertex* b) {
            return a->range.end < b->range.begin;
        });
        *prev = Vertex(toExtend[0], toExtend[1]);
        *next = Vertex(toExtend[2], toExtend[3]);
        *curr = Vertex(prev, next);
    }
};


void PersistentSegmentTree::pushBack(int value) {
    int idx = (int) roots.size();
    if (idx == 0) {
        roots.push_back(addVertex(new Vertex(value, idx)));
        return;
    }
    vector<Vertex*> newPath = path(value, roots[(size_t)(idx - 1)]);
    Vertex* leaf = newPath.back();
    newPath.pop_back();
    Vertex* newRoot = update(value, newPath, idx, leaf);
    roots.push_back(newRoot);
}


int PersistentSegmentTree::prevInRange(int idx, int lo, int hi) const {
    if (idx < 0) return -1;
    if (lo > hi) return -1;
    if (roots.empty()) return -1;
    idx = min(idx, (int) roots.size() - 1);
    vector<Vertex*> minPath = path(lo, roots[(size_t) idx]);
    vector<Vertex*> maxPath = path(hi, roots[(size_t) idx]);
    int result = -1;
    int size = (int) min(minPath.size(), maxPath.size());
    for (int i = 0; i < size; i++) {
        if (minPath[(size_t) i] != maxPath[(size_t) i]) {
            result = i;
            break;
        }
    }
    if (result == -1) {
        return (lo <= minPath.back()->range.begin && hi >= minPath.back()->range.begin) ? minPath.back()->hIdx : -1;
    }
    vector<Vertex*> verticesToProcess;
    for (int i = result; i < (int) minPath.size() - 1; i++) {
        if (lo > minPath[(size_t) i]->leftChild->range.end) continue;
        verticesToProcess.push_back(minPath[(size_t) i]->rightChild);
    }
    if (lo <= minPath.back()->range.begin) verticesToProcess.push_back(minPath.back());
    for (int i = result; i < (int) maxPath.size() - 1; i++) {
        if (hi < maxPath[(size_t) i]->range.begin) break;
        if (hi <= maxPath[(size_t) i]->leftChild->range.end) continue;
        verticesToProcess.push_back(maxPath[(size_t) i]->leftChild);
    }
    if (hi >= maxPath.back()->range.end) {
        verticesToProcess.push_back(maxPath.back());
    }
    result = -1;
    for (int i = 0; i < (int) verticesToProcess.size(); i++) {
        result = max(result, verticesToProcess[(size_t)i]->hIdx);
    }
    return result;
}


void PersistentSegmentTree::done() {
    roots.clear();
    vertices.clear();
}


static PersistentSegmentTree persistentSegmentTree;


void init(const vector<int> &seq) {
    for (int x: seq) persistentSegmentTree.pushBack(x);
}


int prevInRange(int i, int lo, int hi) {
    return persistentSegmentTree.prevInRange(i, lo, hi);
}


void pushBack(int value) {
    persistentSegmentTree.pushBack(value);
}


void done() {
    persistentSegmentTree.done();
}
#include<iostream>
#include<vector>
#include<queue>

using namespace std;

class Iterator {
public:
    virtual bool hasNext() = 0;
    virtual int next() = 0;
    virtual ~Iterator() = default;
};

class BasicIterator : public Iterator {
private:
    vector<int> data;
    int currIdx;

public:
    BasicIterator(vector<int>& data) : data(data), currIdx(0) {}

    bool hasNext() override {
        return currIdx < data.size();
    }

    int next() override {
        if(!hasNext()) {
            throw runtime_error("Out of bounds access");
        }
        return data[currIdx++];
    }

};

class RangeIterator : public Iterator { // [start, ... end)
private:
    int start, end, step, currVal;

public:
    RangeIterator(int start, int end, int step) {
        if(step == 0) {
            throw runtime_error("Step size cannot be 0");
        }
        this->start = start;
        this->end = end;
        this->step = step;
        this->currVal = start;
        if((step > 0 && start > end) || (step < 0 && start < end)) {
            this->currVal = end;
        }
    }

    bool hasNext() override {
        return step > 0 ? currVal < end : currVal > end;
    }

    int next() override {
        if(!hasNext()) {
            throw runtime_error("out of range access");
        }
        int ans = currVal;
        currVal += step;
        return ans;
    }
};

class ZigZagIterator {
private:
    queue<pair<vector<int>::iterator, vector<int>::iterator>> queuedData;

public:
    ZigZagIterator(vector<vector<int>>& data) {
        for(auto& vec : data) {
            if(vec.size() != 0) {
                queuedData.push({vec.begin(), vec.end()});
            }
        }
    }

    bool hasNext() {
        return !queuedData.empty();
    }
    
    int next() {
        if(!hasNext()) {
            throw runtime_error("no next element present");
        }
        auto [it1, itEnd] = queuedData.front();
        queuedData.pop();
        int val = *it1;
        it1++;
        if(it1 != itEnd) {
            queuedData.push({it1, itEnd});
        }
        return val;
    }
};

class ZigZagIteratorOfIterators {
    queue<Iterator*> queuedIters;

    ZigZagIteratorOfIterators(vector<Iterator*>& iterators) {
        for(auto iter : iterators) {
            if(iter->hasNext()) {
                queuedIters.push(iter);
            }
        }
    }

    bool hasNext() {
        return !queuedIters.empty();
    }

    int next() {
        auto it = queuedIters.front();
        queuedIters.pop();
        int val = it->next();
        if (it->hasNext()) {
            queuedIters.push(it);
        }
        return val;
    }
};

int main() {
    vector<int> data1 = {1, 2, 3, 4, 5};
    BasicIterator basicIter = BasicIterator(data1);
    while(basicIter.hasNext()) {
        cout << basicIter.next() << " ";
    }
    cout << endl;

    RangeIterator rangeIter1 = RangeIterator(1, 10, 1);
    while(rangeIter1.hasNext()) {
        cout << rangeIter1.next() << " ";
    }
    cout << endl;

    RangeIterator rangeIter2 = RangeIterator(1, 10, -1);
    while(rangeIter2.hasNext()) {
        cout << rangeIter2.next() << " ";
    }
    cout << endl;

    RangeIterator rangeIter3 = RangeIterator(1, 10, 2);
    while(rangeIter3.hasNext()) {
        cout << rangeIter3.next() << " ";
    }
    cout << endl;

    RangeIterator rangeIter4 = RangeIterator(10, 1, 1);
    while(rangeIter4.hasNext()) {
        cout << rangeIter4.next() << " ";
    }
    cout << endl;

    RangeIterator rangeIter5 = RangeIterator(10, 1, -2);
    while(rangeIter5.hasNext()) {
        cout << rangeIter5.next() << " ";
    }
    cout << endl;

    vector<vector<int>> data2 = {
        {1, 2, 3, 4, 5},
        {6, 7, 8},
        {},
        {9, 10, 11, 12}
    };
    ZigZagIterator zigZagIter = ZigZagIterator(data2);
    while(zigZagIter.hasNext()) {
        cout << zigZagIter.next() << " ";
    }
    cout << endl;
}
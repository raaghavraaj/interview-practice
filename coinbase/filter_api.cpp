#include<iostream>
#include<vector>
using namespace std;

struct Transaction {
    int timestamp, id, userId, currency, amount;
    void printTxn() {
        cout << "Time: " << timestamp <<", Id: " << id << ", userId: " << userId << ", Currency: " << currency << ", Amount: " << amount << endl;
    }
    
};

struct Filter {
    virtual bool matches(Transaction txn) const {
        return true;
    }
    virtual ~Filter() = default;
};

struct TimeRangeFilter : public Filter {
    int startTime, endTime;
    TimeRangeFilter(int start, int end) : startTime(start), endTime(end) {}
    bool matches(Transaction txn) const override {
        if(startTime <= txn.timestamp && endTime >= txn.timestamp) {
            return true;
        }
        return false;
    }
};

struct UserIdFilter : public Filter {
    int userId;
    UserIdFilter(int userId) : userId(userId) {}
    bool matches(Transaction txn) const override {
        return txn.userId == userId;
    }
};

struct CurrencyFilter : public Filter {
    int currency;
    CurrencyFilter(int currency) : currency(currency) {}
    bool matches(Transaction txn) const override {
        return txn.currency == currency;
    }
};

struct GetFilteredTxnRequest {
    vector<Transaction> txns;
    vector<Filter*> filters;
    int cursorTimeStamp;
    int cursorTxnId;
    int pageSize;
};

struct GetFilteredTxnResponse {
    vector<Transaction> txns;
    int cursorTimeStamp;
    int cursorTxnId;
};

GetFilteredTxnResponse getFilteredTxn(GetFilteredTxnRequest request) {
    vector<Transaction> filteredTxns;
    int count = 0;
    for(auto txn : request.txns) {
        if(txn.timestamp < request.cursorTimeStamp || (txn.timestamp == request.cursorTimeStamp && txn.id <= request.cursorTxnId)) {
            continue;
        }
        bool matchesAll = true;
        for(auto filter : request.filters) {
            if(!filter->matches(txn)) {
                matchesAll = false;
                break;
            }
        }
        if(!matchesAll) {
            continue;
        }
        filteredTxns.push_back(txn);
        count++;
        if(count == request.pageSize) {
            break;
        }
    }
    
    if(filteredTxns.empty()) {
        return {{}, request.cursorTimeStamp, request.cursorTxnId};
    }

    int lastPageCursorTimeStamp = filteredTxns.back().timestamp;
    int lastPageCursorTxnId = filteredTxns.back().id;
    return {filteredTxns, lastPageCursorTimeStamp, lastPageCursorTxnId};
};

int main() {        
    vector<Transaction> data = {
        {1, 100, 1, 1, 100},
        {1, 2, 2, 1, 150},
        {2, 3, 3, 2, 200},
        {2, 99, 4, 2, 250},
        {3, 4, 5, 1, 300},
        {3, 5, 6, 2, 350},
        {4, 1, 7, 1, 400},
        {4, 6, 8, 2, 450},
        {5, 7, 9, 1, 500},
        {6, 8, 10, 2, 550},
        {6, 9, 11, 1, 600}
    };
    vector<Filter*> filters = {
        new TimeRangeFilter(2, 4),
        new UserIdFilter(3)
    };
    GetFilteredTxnResponse response1 = getFilteredTxn({data, filters, 0, 0, 4});
    for(auto txn : response1.txns) {
        txn.printTxn();
    }
    cout << endl;

    GetFilteredTxnResponse response2 = getFilteredTxn({data, filters, response1.cursorTimeStamp, response1.cursorTxnId, 4});
    for(auto txn : response2.txns) {
        txn.printTxn();
    }
    cout << endl;
    for (auto filter : filters) {
        delete filter;
    }
}
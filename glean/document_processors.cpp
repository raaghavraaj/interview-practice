#include<bits/stdc++.h>
#include<set>
#include<iostream>
using namespace std;

struct Document {
    int timestamp, idx;
    bool processed;
    bool operator<(const Document& other) const {
        return timestamp > other.timestamp;  // Min-heap based on x
    }
};

void processDocuments(int m, int k, vector<int>& queue_time, vector<int>& processing_time) {
    set<int> available_indexers;
    for(int i = 0; i<m; i++) {
        available_indexers.insert(i);
    }

    priority_queue<Document> pq;
    for(int i = 0; i < queue_time.size(); i++) {
        pq.push({queue_time[i], i, false});
    }

    vector<int> documents_processed(m, 0);
    while(!pq.empty()) {
        Document doc = pq.top();
        pq.pop();
        if(doc.processed) {
            available_indexers.insert(doc.idx);
            continue;
        }

        auto indexer_ptr = available_indexers.lower_bound(doc.idx % m);
        indexer_ptr = indexer_ptr == available_indexers.end() ? available_indexers.lower_bound(0) : indexer_ptr;
        if(indexer_ptr == available_indexers.end()) {
            continue;
        }

        int indexer = *indexer_ptr;
        documents_processed[indexer]++;
        pq.push({doc.timestamp + processing_time[doc.idx], indexer, true});
    }

    int total_documents_processed = 0;
    priority_queue<pair<int, int>, vector<pair<int, int>>, greater<>> most_working_indexers_pq;
    for(int i = 0; i < m;  i++) {
        total_documents_processed += documents_processed[i];
        most_working_indexers_pq.push({documents_processed[i], i});
        if(most_working_indexers_pq.size() > k) {
            most_working_indexers_pq.pop();
        }
    }

    vector<pair<int, int>> most_working_indexers;
    while(!most_working_indexers_pq.empty()) {
        auto top = most_working_indexers_pq.top();
        most_working_indexers_pq.pop();
        most_working_indexers.push_back(top);
    }

    cout << "Total documents processed = " << total_documents_processed << endl;
    cout << "Most worked indexers are:" << endl;
    for(int i = most_working_indexers.size() - 1; i >= 0; i--) {
        cout << most_working_indexers[i].second <<": " <<  most_working_indexers[i].first << " Documents" << endl;
    }
}

int main() {
    int m = 3, k = 2;
    vector<int> queue_time = {1,2,3,7}, processing_time = {5,4,3,2};
    processDocuments(m, k, queue_time, processing_time);
}
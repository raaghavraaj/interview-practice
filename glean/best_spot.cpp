#include<bits/stdc++.h>
#include<iostream>
using namespace std;

void updateDistances(pair<int, int> startPos, vector<vector<char>>& room, vector<vector<int>>& distance) {
    vector<int> dx = {-1, 1, 0, 0};
    vector<int> dy = {0, 0, -1, 1};
    int N = room.size(), M = room[0].size();

    queue<pair<int, int>> q;
    vector<vector<bool>> visited(N, vector<bool>(M, false));
    q.push(startPos);
    visited[startPos.first][startPos.second] = true;
    int steps  = 0;
    while(!q.empty()) {
        steps++;
        int qSize = q.size();
        for(int i = 0; i < qSize; i++) {
            auto currPos = q.front();
            for(int j = 0; j < 4; j++) {
                int x = currPos.first + dx[j], y = currPos.second + dy[j];
                if(x >= 0 && x < N && y >= 0 && y < M && room[x][y] != '#' && !visited[x][y]) {
                    distance[x][y]+= steps;
                    q.push({x, y});
                    visited[x][y] = true;
                }
            }
            q.pop();
        }
    }
}

pair<int, int> findBestSpot(vector<vector<char>>& room, int K) {
    int N = room.size(), M = room[0].size();
    vector<vector<int>> distance(N, vector<int>(M, 0));
    for(int i = 0; i < N; i++) {
        for(int j = 0; j < M; j++) {
            if(room[i][j] == '*') {
                updateDistances({i, j}, room, distance);
            }
        }
    }

    int minDist = INT_MAX;
    pair<int, int> bestSpot = {-1, -1};
    for(int i = 0; i < N; i++) {
        for(int j = 0; j < M; j++) {
            if(distance[i][j] < minDist && room[i][j] != '#') {
                minDist = distance[i][j];
                bestSpot = {i, j};
            }
        }
    }
    return bestSpot;
}

int main() {
    
}
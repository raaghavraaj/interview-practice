#include<bits/stdc++.h>
#include<iostream>
#include <iomanip>
using namespace std;

class Connect4 {
private:
    int rows, cols;
    vector<vector<char>> board;
    char currentPlayer;

    int countDirection(int row, int col, int deltaRow, int deltaCol) {
        int count = 0;
        int r = row + deltaRow;
        int c = col + deltaCol;
        while (r >= 0 && r < rows && c >= 0 && c < cols && board[r][c] == currentPlayer) {
            count++;
            r += deltaRow;
            c += deltaCol;
        }
        return count;
    }

public:
    Connect4(int n = 6, int m = 7) : rows(n), cols(m), board(n, vector<char>(m, '.')), currentPlayer('X') {}

    void displayBoard() {
        for (const auto& row : board) {
            for (char cell : row) {
                cout << setw(2) << cell << " ";
            }
            cout << endl;
        }
        for (int i = 0; i < cols; i++) {
            cout << setw(2) << i << " ";
        }
        cout << endl;
    }

    void resetBoard() {
        for (auto& row : board) {
            fill(row.begin(), row.end(), '.');
        }
        currentPlayer = 'X';
    }

    bool makeMove(int col) {
        if (!isValidMove(col)) return false;

        int row = getAvailableRow(col);
        board[row][col] = currentPlayer;
        
        return checkWin(row, col);
    }

    int getAvailableRow(int col) {
        for (int i = rows - 1; i >= 0; i--) {
            if (board[i][col] == '.') return i;
        }
        return -1;
    }

    bool isValidMove(int col) {
        return col >= 0 && col < cols && board[0][col] == '.';
    }

    void switchPlayer() {
        currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
    }

    bool checkWin(int row, int col) {
        // Horizontal
        if (1 + countDirection(row, col, 0, -1) + countDirection(row, col, 0, 1) >= 4)
            return true;
        // Vertical
        if (1 + countDirection(row, col, -1, 0) + countDirection(row, col, 1, 0) >= 4)
            return true;
        // Diagonal top right to bottom left
        if (1 + countDirection(row, col, -1, 1) + countDirection(row, col, 1, -1) >= 4)
            return true;
        // Diagonal top left to bottom right
        if (1 + countDirection(row, col, -1, -1) + countDirection(row, col, 1, 1) >= 4)
            return true;

        return false;
    }

    bool isBoardFull() {
        for (int i = 0; i < cols; i++) {
            if (board[0][i] == '.') return false;
        }
        return true;
    }

    char getCurrentPlayer() {
        return currentPlayer;
    }
};

int main() {
    Connect4* game = new Connect4(6, 7);
    bool gameWon = false;

    cout << "Welcome to Connect 4!\n";
    game->displayBoard();

    while (true) {
        int col;
        cout << "Player " << game->getCurrentPlayer() << ", enter your move (0-" << 6 << "): ";
        cin >> col;

        if (!cin || !game->isValidMove(col)) {
            cin.clear(); // Clear error flags if invalid input
            cin.ignore(1000, '\n'); // Discard bad input
            cout << "Invalid move. Try again.\n";
            continue;
        }

        bool win = game->makeMove(col);
        game->displayBoard();

        if (win) {
            cout << "Player " << game->getCurrentPlayer() << " wins!\n";
            break;
        }

        if (game->isBoardFull()) {
            cout << "It's a draw!\n";
            break;
        }

        game->switchPlayer();
    }
}
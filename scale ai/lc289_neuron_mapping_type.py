def count_live_neighbors(board, i, j):
    cnt = 0
    for dx in [-1, 0, 1]:
        for dy in [-1, 0, 1]:
            if dx == 0 and dy == 0:
                continue
            if len(board) > i + dx >= 0 and len(board[0]) > j + dy >= 0 and (board[i+dx][j+dy] in [1, 2]):
                cnt+=1
    return cnt

def convert_in_place(board):
    for i in range(len(board)):
        for j in range(len(board[0])):
            live_neighbours = count_live_neighbors(board, i, j)
            if board[i][j] == 1:
                if live_neighbours not in [2, 3]:
                    board[i][j] = 2
            else:
                if live_neighbours == 3:
                    board[i][j] = 3
    
    for i in range(len(board)):
        for j in range(len(board[0])):
            board[i][j] = board[i][j] % 2
    print(board)
    return board

if __name__ == '__main__':
    board = [[0,1,0],[0,0,1],[1,1,1],[0,0,0]]
    board = convert_in_place(board)
    print(board)
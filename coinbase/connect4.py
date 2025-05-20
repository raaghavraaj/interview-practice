class Connect4:
    def __init__(self, n: int, m: int):
        self.rows = n
        self.columns = m
        self.board = [['_' for _ in range(self.columns)] for _ in range(self.rows)]
        self.currentPlayer = 'X'

    def __count_direction(self, row: int, col: int, delta_row: int, delta_col: int) -> int:
        count = 0
        r = row + delta_row
        c = col + delta_col
        while r >= 0 and r < self.rows and c >=0 and c < self.columns and self.board[r][c] == self.currentPlayer:
            count+=1
            r += delta_row
            c += delta_col
        return count
    
    def __check_win(self, row: int, col: int) -> bool:
        if 1 + self.__count_direction(row, col, 0, -1) + self.__count_direction(row, col, 0, 1) >= 4:
            return True
        
        if 1 + self.__count_direction(row, col, -1, 0) + self.__count_direction(row, col, 1, 0) >= 4:
            return True
        
        if 1 + self.__count_direction(row, col, -1, 1) + self.__count_direction(row, col, 1, -1) >= 4:
            return True
        
        if 1 + self.__count_direction(row, col, -1, -1) + self.__count_direction(row, col, 1, 1) >= 4:
            return True

        return False
    
    def __get_available_row(self, col) -> int:
        for i in range(self.rows - 1, -1, -1):
            if self.board[i][col] == '_':
                return i
    
    def display_board(self):
        for row in self.board:
            for cell in row:
                print(f"{cell:>2}", end=" ")
            print()
        
        for i in range(self.columns):
            print(f"{i:>2}", end=" ")
        print()

    def reset_board(self):
        self.board = [['_' for _ in range(self.columns)] for _ in range(self.rows)]
    
    def is_valid_move(self, col: int):
        return col >= 0 and col < self.columns and self.board[0][col] == '_'
    
    def make_move(self, col: int) -> bool:
        if not self.is_valid_move(col):
            return False
        
        r = self.__get_available_row(col)
        self.board[r][col] = self.currentPlayer
        return self.__check_win(r, col)
    
    def is_board_full(self) -> bool:
        for cell in self.board[0]:
            if cell == '_':
                return False
        return True
    
    def switch_player(self):
        self.currentPlayer =  'O' if self.currentPlayer == 'X' else 'X'
    
    def get_current_player(self):
        return self.currentPlayer
    
if __name__ == '__main__':
    game = Connect4(6, 7)
    print("Welcome to Connect 4!")
    game.display_board()

    while True:
        try:
            col = int(input(f"Player {game.get_current_player()}, enter your move (0-6): "))
        except ValueError:
            print("Invalid input. Please enter a number.")
            continue

        if not game.is_valid_move(col):
            print("Invalid move. Try again.")
            continue

        win = game.make_move(col)
        game.display_board()

        if win:
            print(f'Player {game.get_current_player()} wins the game!')
            break

        if game.is_board_full():
            print("Its a draw!")
            break

        game.switch_player()
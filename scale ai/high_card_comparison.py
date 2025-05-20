from enum import Enum
import random
from typing import List

class Suit(Enum):
    CLUBS = 'CLUBS'
    DIAMONDS = 'DIAMONDS'
    HEARTS = 'HEARTS'
    SPADES = 'SPADES'

class Rank(Enum):
    ACE = 1
    TWO = 2
    THREE = 3
    FOUR = 4
    FIVE = 5
    SIX = 6
    SEVEN = 7
    EIGHT = 8
    NINE = 9
    TEN = 10
    JACK = 11
    QUEEN = 12
    KING = 13

class Card:
    def __init__(self, suit: Suit, rank: Rank):
        self.suit = suit
        self.rank = rank

    def __repr__(self):
        return f"{self.rank.name} of {self.suit.value}"

    def __lt__(self, other: 'Card'):
        return self.rank.value < other.rank.value

    def __eq__(self, other: 'Card'):
        return self.rank.value == other.rank.value

class Hand:
    def __init__(self, cards: List[Card]):
        self.cards = cards
    
    def __repr__(self):
        return ", ".join(str(card) for card in self.cards)

    def best_card(self):
        return max(self.cards)

class Deck:
    def __init__(self):
        self.cards = [Card(suit, rank) for rank in Rank for suit in Suit]
    
    def shuffle(self):
        random.shuffle(self.cards)
    
    def draw(self, num: int) -> Hand:
        drawn = self.cards[:num]
        self.cards = self.cards[num:]
        return Hand(drawn)


def play_game():
    deck = Deck()
    deck.shuffle()

    hand1 = deck.draw(5)
    hand2 = deck.draw(5)

    print("Player 1 hand:", hand1)
    print("Player 2 hand:", hand2)

    card1 = hand1.best_card()
    card2 = hand2.best_card()

    print("Player 1 best card:", card1)
    print("Player 2 best card:", card2)

    if card1 > card2:
        print("Player 1 wins!")
    elif card1 < card2:
        print("Player 2 wins!")
    else:
        print("It's a tie!")

if __name__ == "__main__":
    play_game()
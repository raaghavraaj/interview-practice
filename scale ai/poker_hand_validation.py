from enum import Enum, IntEnum
from typing import List
from collections import Counter

class Suit(Enum):
    CLUBS = 'CLUBS'
    DIAMONDS = 'DIAMONDS'
    HEARTS = 'HEARTS'
    SPADES = 'SPADES'

class Rank(IntEnum):
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

def is_straight(ranks: list[int]) -> bool:
    unique_ranks = sorted(set(ranks))
    if len(unique_ranks) != 5:
        return False

    return unique_ranks[-1] - unique_ranks[0] == 4

def checkHandValidity(cards: List[Card]) -> bool:
    cards_set = set([(card.suit.name, card.rank.name) for card in cards])
    if len(cards_set) != 5 or len(set(cards_set)) != 5:
        print("Not a valid hand")
        return False

    ranks = [card.rank.value for card in cards]
    suits = [card.suit.name for card in cards]

    rank_counts = Counter(ranks)
    suit_counts = Counter(suits)

    is_flush = len(suit_counts) == 1
    is_straight_hand = is_straight(ranks)

    if is_flush and is_straight_hand:
        print("Straight Flush")
        return True
    elif 5 in rank_counts.values():
        print("Five of a Kind")
        return True
    elif 4 in rank_counts.values():
        print("Four of a Kind")
        return True
    elif sorted(rank_counts.values()) == [2, 3]:
        print("Full House")
        return True
    elif is_flush:
        print("Flush")
        return True
    elif is_straight_hand:
        print("Straight")
        return True
    else:
        print("Not a valid hand")
        return False



if __name__ == '__main__':
    cards = [
        Card(Suit.HEARTS, Rank.TEN),
        Card(Suit.HEARTS, Rank.JACK),
        Card(Suit.HEARTS, Rank.QUEEN),
        Card(Suit.HEARTS, Rank.KING),
        Card(Suit.HEARTS, Rank.ACE),
    ]

    checkHandValidity(cards)
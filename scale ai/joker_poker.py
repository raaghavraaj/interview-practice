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
    def __init__(self, suit: Suit = None, rank: Rank = None, is_joker: bool = False):
        self.suit = suit
        self.rank = rank
        self.is_joker = is_joker

    def __repr__(self):
        return "JOKER" if self.is_joker else f"{self.rank.name} of {self.suit.name}"

def is_straight_with_jokers(ranks: List[int], jokers: int) -> bool:
    unique_ranks = sorted(set(ranks))
    gaps = 0
    for i in range(1, len(unique_ranks)):
        gap = unique_ranks[i] - unique_ranks[i - 1] - 1
        if gap > 0:
            gaps += gap

    return gaps <= jokers and len(unique_ranks) + jokers >= 5

def checkHandValidity(cards: List[Card]) -> bool:
    jokers = sum(1 for card in cards if card.is_joker)
    non_joker_cards = [card for card in cards if not card.is_joker]

    cards_set = set((card.suit.name, card.rank.name) for card in non_joker_cards)
    if len(cards_set) + jokers != 5:
        print("Not a valid hand")
        return False

    ranks = [card.rank.value for card in non_joker_cards]
    suits = [card.suit.name for card in non_joker_cards]

    rank_counts = Counter(ranks)
    suit_counts = Counter(suits)

    is_flush = len(suit_counts) == 1 or len(suit_counts) + jokers == 1
    is_straight_hand = is_straight_with_jokers(ranks, jokers)

    counts = sorted(rank_counts.values(), reverse=True)
    if jokers > 0:
        counts.append(jokers)
        counts = sorted(counts, reverse=True)

    if is_flush and is_straight_hand:
        print("Straight Flush")
        return True
    elif counts[0] >= 5:
        print("Five of a Kind")
        return True
    elif counts[0] == 4:
        print("Four of a Kind")
        return True
    elif counts[0] == 3 and counts[1] >= 2:
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
        Card(is_joker=True)
    ]

    checkHandValidity(cards)
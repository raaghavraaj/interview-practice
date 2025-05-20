from dataclasses import dataclass
from typing import List

@dataclass
class Transaction:
    timestamp: int
    id: int
    userId: int
    currency: int
    amount: int

    def print_txn(self):
        print(f"Time: {self.timestamp}, Id: {self.id}, userId: {self.userId}, Currency: {self.currency}, Amount: {self.amount}")

class Filter:
    def matches(self, txn: Transaction) -> bool:
        return True

class TimeRangeFilter(Filter):
    def __init__(self, start: int, end: int):
        self.start = start
        self.end = end

    def matches(self, txn: Transaction) -> bool:
        return self.start <= txn.timestamp <= self.end

class UserIdFilter(Filter):
    def __init__(self, user_id: int):
        self.user_id = user_id

    def matches(self, txn: Transaction) -> bool:
        return txn.userId == self.user_id

class CurrencyFilter(Filter):
    def __init__(self, currency: int):
        self.currency = currency

    def matches(self, txn: Transaction) -> bool:
        return txn.currency == self.currency

@dataclass
class GetFilteredTxnRequest:
    txns: List[Transaction]
    filters: List[Filter]
    cursor_timestamp: int
    cursor_txn_id: int
    page_size: int

@dataclass
class GetFilteredTxnResponse:
    txns: List[Transaction]
    cursor_timestamp: int
    cursor_txn_id: int

def get_filtered_txn(request: GetFilteredTxnRequest) -> GetFilteredTxnResponse:
    filtered_txns = []
    count = 0

    for txn in request.txns:
        if txn.timestamp < request.cursor_timestamp or \
           (txn.timestamp == request.cursor_timestamp and txn.id <= request.cursor_txn_id):
            continue

        if not all(filter.matches(txn) for filter in request.filters):
            continue

        filtered_txns.append(txn)
        count += 1
        if count == request.page_size:
            break

    if not filtered_txns:
        return GetFilteredTxnResponse([], request.cursor_timestamp, request.cursor_txn_id)

    last = filtered_txns[-1]
    return GetFilteredTxnResponse(filtered_txns, last.timestamp, last.id)

# Example usage
if __name__ == "__main__":
    data = [
        Transaction(1, 100, 1, 1, 100),
        Transaction(1, 2, 2, 1, 150),
        Transaction(2, 3, 3, 2, 200),
        Transaction(2, 99, 4, 2, 250),
        Transaction(3, 4, 5, 1, 300),
        Transaction(3, 5, 6, 2, 350),
        Transaction(4, 1, 7, 1, 400),
        Transaction(4, 6, 8, 2, 450),
        Transaction(5, 7, 9, 1, 500),
        Transaction(6, 8, 10, 2, 550),
        Transaction(6, 9, 11, 1, 600)
    ]

    filters = [TimeRangeFilter(2, 4), UserIdFilter(3)]

    request1 = GetFilteredTxnRequest(data, filters, 0, 0, 4)
    response1 = get_filtered_txn(request1)
    for txn in response1.txns:
        txn.print_txn()
    print()

    request2 = GetFilteredTxnRequest(data, filters, response1.cursor_timestamp, response1.cursor_txn_id, 4)
    response2 = get_filtered_txn(request2)
    for txn in response2.txns:
        txn.print_txn()

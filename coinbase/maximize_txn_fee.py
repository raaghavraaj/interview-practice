from dataclasses import dataclass
from typing import List

@dataclass
class Transaction:
    tx_id: int
    fee: int
    size: int

class TransactionPool:
    def __init__(self, transactions: List[Transaction]):
        self.tx_map = {tx.tx_id: tx for tx in transactions}
        self.transactions = transactions

    def get_transaction(self, tid):
        return self.tx_map.get(tid)

    def all_transactions(self):
        return self.transactions

class TransactionsSelector:
    def __init__(self, txn_pool: TransactionPool, max_block_size: int):
        self.pool = txn_pool
        self.max_block_size = max_block_size
        self.used_size = 0
        self.selected = []
        self.added = set()

    def select_transactions(self) -> List[Transaction]:
        sorted_txns = sorted(self.pool.all_transactions(), key=lambda txn: txn.fee/txn.size, reverse=True)
        for txn in sorted_txns:
            if self.used_size + txn.size <= self.max_block_size:
                self.__add_transaction(txn)
        return self.selected
    
    def __add_transaction(self, txn: Transaction):
        if txn.tx_id in self.added:
            return
        self.selected.append(txn)
        self.added.add(txn.tx_id) 
        self.used_size += txn.size
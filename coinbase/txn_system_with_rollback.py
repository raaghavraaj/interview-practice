def process_txns(balances, txns):
    amount_transfers = []
    for txn in txns:
        delta_amt = balances[txn[0]] * (txn[2] / 100)
        amount_transfers.append(delta_amt)
        balances[txn[0]] = balances[txn[0]] - delta_amt
        balances[txn[1]] = balances[txn[1]] + delta_amt
    return amount_transfers

def rollback(balances, amount_transfers, txns, k):
    l = len(amount_transfers)
    for i in range(l - k):
        val = amount_transfers.pop()
        balances[txns[l-i-1][0]] += val
        balances[txns[l-i-1][1]] -= val
    
    for i in range(k+1, len(txns)):
        delta_amt = balances[txns[i][0]] * (txns[i][2] / 100)
        amount_transfers.append(delta_amt)
        balances[txns[i][0]] = balances[txns[i][0]] - delta_amt
        balances[txns[i][1]] = balances[txns[i][1]] + delta_amt

if __name__ == '__main__':
    balances = {
        'A': 100,
        'B': 80,
        'C': 150,
        'D': 180
    }

    txns = [('A', 'B', 10), ('B', 'C', 20), ('C', 'A', 30), ('D', 'A', 50)]

    amount_transfers = process_txns(balances, txns)
    print(balances)
    rollback(balances, amount_transfers, txns, 2)
    print(balances)
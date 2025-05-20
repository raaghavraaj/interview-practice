from collections import deque
import heapq

def process_orders(orders):
    max_heap = []
    total_gain = 0
    sell_pnl = []
    available_stocks = 0
    for order in orders:
        if order['type'] == 'buy':
            heapq.heappush(max_heap, (-order['price'], -order['amount']))
            available_stocks += order['amount']
        else:
            amount_to_sell = order['amount']
            sell_price = order['price']
            gain = 0
            if available_stocks < amount_to_sell:
                sell_pnl.append("Invalid Sell Txn")
                continue
            available_stocks -= amount_to_sell
            while amount_to_sell != 0 and len(max_heap) != 0:
                buy_price, stock_cnt = heapq.heappop(max_heap)
                if -stock_cnt > amount_to_sell :
                    gain += amount_to_sell * (sell_price + buy_price)
                    heapq.heappush(max_heap, (buy_price, stock_cnt + amount_to_sell))
                    # q.appendleft((stock_cnt - amount_to_sell, buy_price))
                    amount_to_sell = 0
                elif -stock_cnt == amount_to_sell:
                    gain += amount_to_sell * (sell_price + buy_price)
                    amount_to_sell = 0
                else:
                    gain += -stock_cnt * (sell_price + buy_price)
                    amount_to_sell += stock_cnt
            sell_pnl.append(gain)
            total_gain += gain
    return sell_pnl, total_gain

if __name__ == "__main__":
    orders = [
        {'date': 1, 'type': 'buy', 'amount': 5, 'price': 25},
        {'date': 2, 'type': 'buy', 'amount': 10, 'price': 40},
        {'date': 2.5, 'type': 'sell', 'amount': 20, 'price': 6},
        {'date': 3, 'type': 'sell', 'amount': 2, 'price': 6},
        {'date': 4, 'type': 'sell', 'amount': 6, 'price': 12}
    ]
    sell_pnl, total_gain = process_orders(orders)
    print(sell_pnl)
    print(total_gain)
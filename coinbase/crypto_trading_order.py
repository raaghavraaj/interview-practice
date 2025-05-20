from dataclasses import dataclass
from enum import Enum
from typing import List
import time

class OrderState(Enum):
    LIVE = "Live"
    PAUSE = "Pause"
    COMPLETED = "Completed"
    CANCEL = "Cancel"

@dataclass
class OrderInfo:
    order_id: int
    currency: str
    amount: int
    timestamp: int
    buy: bool
    state: OrderState

class CryptoTradingSystem:
    def __init__(self):
        self.orders = {}
        self.live_orders = set()
        self.paused_orders = set()
        self.canceled_orders = set()
        self.completed_orders = set()
        self.current_order_id = 0

    def place_order(self, user_id: int, currency: str, amount: int):
        self.current_order_id += 1
        order_info = OrderInfo(self.current_order_id, currency, amount, time.time, True, OrderState.LIVE)
        self.orders[self.current_order_id] = order_info
        self.live_orders.add(self.current_order_id)

    def display_live_orders(self) -> List[OrderInfo]:
        live_orders = []
        for order_id in self.live_orders:
            live_orders.append(self.orders[order_id])
        return live_orders

    def pause_order(self, order_id: int):
        if order_id in self.live_orders:
            self.orders[order_id].state = OrderState.PAUSE
            self.live_orders.remove(order_id)
            self.paused_orders.add(order_id)
            

    def resume_order(self, order_id: int):
        if order_id in self.paused_orders:
            self.orders[order_id].state = OrderState.LIVE
            self.paused_orders.remove(order_id)
            self.live_orders.add(order_id)

    def cancel_order(self, order_id: int):
        if order_id in self.live_orders or order_id in self.paused_orders:
            self.canceled_orders.add(order_id)
            self.paused_orders.remove(order_id)
            self.live_orders.remove(order_id)
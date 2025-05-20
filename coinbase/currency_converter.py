import json
from collections import defaultdict

class CurrencyExchange:
    def __init__(self, exchange_rates):
        self.graph = self._build_graph(exchange_rates)
        
    def _build_graph(self, exchange_rates):
        graph = defaultdict(list)
        for pair, rates in exchange_rates.items():
            from_curr, to_curr = pair.split('-')
            ask = rates['ask']
            bid = rates['bid']
            
            # Add forward conversion (using bid price when we're selling the base currency)
            graph[from_curr].append((to_curr, bid, 1/ask))
            
            # Add reverse conversion (using ask price when buying the base currency)
            graph[to_curr].append((from_curr, 1/ask, bid))
            
        return graph
    
    def find_max_conversion(self, start_currency, target_currency, amount):
        # Track the best solution found so far
        self.max_amount = 0
        self.best_path = []
        
        # Start DFS with initial amount and empty path
        self._dfs(start_currency, target_currency, amount, [], set())
        
        return self.max_amount, self.best_path
    
    def _dfs(self, current_currency, target_currency, current_amount, path, visited):
        # Add current currency to path and visited set
        path = path + [current_currency]
        visited.add(current_currency)
        
        # If we've reached the target, check if this is the best conversion
        if current_currency == target_currency:
            if current_amount > self.max_amount:
                self.max_amount = current_amount
                self.best_path = path
            return
        
        # Explore all possible conversions from current currency
        for neighbor, rate, _ in self.graph.get(current_currency, []):
            if neighbor not in visited:
                new_amount = current_amount * rate
                self._dfs(neighbor, target_currency, new_amount, path, visited.copy())
    
    @staticmethod
    def from_json(json_str):
        exchange_rates = json.loads(json_str)
        return CurrencyExchange(exchange_rates)

# Example usage
if __name__ == "__main__":
    # Example exchange rates
    exchange_data = """
    {
        "BTC-USD": {"ask": 1000, "bid": 990},
        "USD-ETH": {"ask": 10, "bid": 8},
        "ETH-BNB": {"ask": 1000, "bid": 990},
        "BTC-EUR": {"ask": 1200, "bid": 1150},
        "ETH-EUR": {"ask": 220, "bid": 210}
    }
    """
    
    # Initialize from JSON
    exchange = CurrencyExchange.from_json(exchange_data)
    
    # Find best conversion from BTC to EUR with 1 BTC
    max_amount, best_path = exchange.find_max_conversion("BTC", "EUR", 1)
    print(f"Maximum EUR: {max_amount:.2f}")
    print("Path:", " -> ".join(best_path))
    
    # Find best conversion from BTC to BNB with 1 BTC
    max_amount, best_path = exchange.find_max_conversion("BTC", "BNB", 1)
    print(f"\nMaximum BNB: {max_amount:.2f}")
    print("Path:", " -> ".join(best_path))
from collections import deque
from typing import List, Dict

def shortest_distance(tree: Dict, start: int, end: int) -> int:
    visited = set()
    q = deque([(start, 0)])

    while q :
        node, distance = q.popleft()
        if node == end:
            return distance
        for v in tree.get(node, []):
            if v not in visited:
                q.append((v, distance + 1))
    return -1

def shortest_distance_multisource_bfs(tree: Dict, a: List[int], b: List[int])  -> int:
    visited = set()
    dist = {}
    queue = deque()

    for node in a:
        queue.append((node, 0))
        visited.add(node)
        dist[node] = 0

    while queue:
        current, d = queue.popleft()
        for neighbor in tree.get(current, []):
            if neighbor not in visited:
                visited.add(neighbor)
                dist[neighbor] = d + 1
                queue.append((neighbor, d + 1))

    return min(dist.get(node, float('inf')) for node in b)

if __name__ == '__main__':
    tree = {
        1: [2, 3],
        2: [1, 4, 5],
        3: [1, 6, 7],
        4: [2],
        5: [2],
        6: [3],
        7: [3, 8],
        8: [7]
    }

    print(shortest_distance(tree, 4, 6))
    
    a = [4, 5, 6]
    b = [7, 8]
    print(shortest_distance_multisource_bfs(tree, a, b))

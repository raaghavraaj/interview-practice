
from collections import deque

class BasicIterator:
    def __init__(self, arr):
        self.list = arr
        self.index = 0
    
    def hasNext(self):
        return self.index < len(self.arr) - 1
    
    def next(self):
        if not self.hasNext():
            raise RuntimeError("Accessing index out of bounds")
        self.index += 1
        return self.arr[self.index]

class RangeIterator:
    def __init__(self, start, end, step):
        if step == 0:
            raise ValueError("step cannot be zero")
        self.start = start
        self.end = end
        self.step = step
        self.current_value = start - step
    
    def hasNext(self):
        return self.current_value + self.step <= self.end if self.step > 0 else self.current_value + self.step >= self.end
    
    def next(self):
        if not self.hasNext():
            raise RuntimeError("Accessing value out of range")
        self.current_value += self.step
        return self.current_value

class InterLeavingIterator:
    def __init__(self, lists):
        self.queue = deque()
        for lst in lists:
            if lst:
                self.queue.append(iter(lst))

    def has_next(self):
        return bool(self.queue)

    def next(self):
        if not self.has_next():
            raise StopIteration()
        it = self.queue.popleft()
        val = next(it)
        try:
            # Try peeking the next item
            peek = next(it)
            self.queue.append(self._prepend(peek, it))
        except StopIteration:
            pass
        return val

    def _prepend(self, val, iterator):
        # Prepend a value to an iterator
        yield val
        yield from iterator
    
lists = [[0, 1, 2], [], [3, 4], [5]]
zigzag = InterLeavingIterator(lists)

result = []
while zigzag.has_next():
    result.append(zigzag.next())

print(result)  # Output: [0, 3, 5, 1, 4, 2]

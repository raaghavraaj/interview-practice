import heapq
import time
import threading

class Event:
    def __init__(self, time, emails, message, is_recurring):
        self.time = time
        self.emails = emails
        self.message = message
        self.is_recurring = is_recurring

    def __lt__(self, other):
        return self.time < other.time

class Calendar:
    def __init__(self):
        self.events = []  # min-heap
        self.next_event_time = None
        self.timeout_handler = None
        self.lock = threading.Lock()

    def add_event(self, event_time, emails, message, is_recurring):
        event = Event(event_time, emails, message, is_recurring)

        with self.lock:
            if self.next_event_time is None:
                heapq.heappush(self.events, event)
                self.schedule_next()
            elif event_time < self.next_event_time and self.timeout_handler is not None:
                heapq.heappush(self.events, event)
                self.timeout_handler.cancel()
                self.schedule_next()
            else:
                heapq.heappush(self.events, event)

    def schedule_next(self):
        if not self.events:
            return

        next_event = self.events[0]  # peek
        current_time = time.time()
        time_diff = max(0, next_event.time - current_time)

        self.next_event_time = next_event.time

        self.timeout_handler = threading.Timer(time_diff, self.consume)
        self.timeout_handler.start()

    def consume(self):
        with self.lock:
            if not self.events:
                return

            event = heapq.heappop(self.events)
            self.send_email(event)

            if event.is_recurring:
                event.time += 86400  # 1 day in seconds
                heapq.heappush(self.events, event)

            if not self.events:
                self.next_event_time = None
                self.timeout_handler = None
            else:
                self.schedule_next()

    def send_email(self, event):
        print(f"Sending email to {event.emails} with message: '{event.message}' at {time.ctime(event.time)}")

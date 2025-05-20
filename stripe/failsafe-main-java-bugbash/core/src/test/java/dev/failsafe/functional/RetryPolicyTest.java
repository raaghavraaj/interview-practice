/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package dev.failsafe.functional;

import dev.failsafe.testing.Testing;
import net.jodah.concurrentunit.Waiter;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import org.junit.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;


public class RetryPolicyTest extends Testing {
  /**
   * Tests a simple execution that does not retry.
   */
  @Test
  public void shouldNotRetry() {
    testGetSuccess(Failsafe.with(RetryPolicy.ofDefaults()), ctx -> true, (f, e) -> {
      assertEquals(1, e.getAttemptCount());
      assertEquals(1, e.getExecutionCount());
    }, true);
  }

  /**
   * Asserts that a non-handled exception does not trigger retries.
   */
  @Test
  public void shouldThrowOnNonRetriableFailure() {
    // Given
    RetryPolicy<Object> retryPolicy = RetryPolicy.builder()
        .withMaxRetries(-1)
        .handle(IOException.class)
        .build();

    // When / Then
    testRunFailure(Failsafe.with(retryPolicy), ctx -> {
      if (ctx.getAttemptCount() < 2)
        throw new IOException();
      throw new IllegalArgumentException();
    }, (f, e) -> assertEquals(3, e.getAttemptCount()), IllegalArgumentException.class);
  }

  /**
   * Asserts that an execution is failed when the max duration is exceeded.
   */
  @Test
  public void shouldCompleteWhenMaxDurationExceeded() {
    Stats stats = new Stats();
    RetryPolicy<Boolean> retryPolicy = withStats(
        RetryPolicy.<Boolean>builder().handleResult(false).withMaxDuration(Duration.ofMillis(100)), stats).build();

    testGetSuccess(stats::reset, Failsafe.with(retryPolicy), ctx -> {
      Testing.sleep(120);
      return false;
    }, (f, e) -> {
      assertEquals(1, e.getAttemptCount());
      assertEquals(1, stats.failureCount);
    }, false);
  }

  /**
   * Asserts that the ExecutionScheduledEvent.getDelay is as expected.
   */
  @Test
  public void assertScheduledRetryDelay() throws Throwable {
    // Given
    Waiter waiter = new Waiter();
    RetryPolicy<Object> rp = RetryPolicy.builder().withDelay(Duration.ofMillis(10)).onRetryScheduled(e -> {
      waiter.assertEquals(e.getDelay().toMillis(), 10L);
      waiter.resume();
    }).build();

    // Sync when / then
    ignoreExceptions(() -> Failsafe.with(rp).run(() -> {
      throw new IOException();
    }));
    waiter.await(1000);

    // Async when / then
    Failsafe.with(rp).runAsync(() -> {
      throw new IOException();
    });
    waiter.await(1000);
  }

  @Test
  public void shouldRetry() {
      AtomicInteger counter = new AtomicInteger();
      RetryPolicy<Object> rp = RetryPolicy.builder()
          .handle(IOException.class)
          .withMaxRetries(2)
        .handleResult("testing")
        .build();

      assertThrows(
          () -> Failsafe.with(rp).get(() -> {
            counter.incrementAndGet();
            throw new IOException();
          })
      );

      assertEquals(3, counter.get());
    }

  @Test
  public void shouldRetry1() {
      AtomicInteger counter = new AtomicInteger();
      RetryPolicy<Object> rp = RetryPolicy.builder()
          .handle(IOException.class)
          .withMaxRetries(2)
          .handleResult(null)
          .build();

      Failsafe.with(rp).get(() -> {
        counter.incrementAndGet();
        return null;
      });

      assertEquals(3, counter.get());
    }

  @Test
  public void shouldRetry2() {
      AtomicInteger counter = new AtomicInteger();
      RetryPolicy<Object> rp = RetryPolicy.builder()
          .handle(IOException.class)
          .withMaxRetries(2)
          .handleResult("test")
          .build();

      Failsafe.with(rp).get(() -> {
        counter.incrementAndGet();
        return "test";
      });

      assertEquals(3, counter.get());
    }

  @Test
  public void shouldRetry3() {
      AtomicInteger counter = new AtomicInteger();
      RetryPolicy<Object> rp = RetryPolicy.builder()
          .withMaxRetries(2)
          .handle(IOException.class)
          .handleResult("testing")
          .build();

      assertThrows(
          () -> Failsafe.with(rp).get(() -> {
            counter.incrementAndGet();
            throw new IOException();
          })
      );

      assertEquals(3, counter.get());
    }

  @Test
  public void shouldRetry4() {
      AtomicInteger counter = new AtomicInteger();
      RetryPolicy<Object> rp = RetryPolicy.builder()
          .withMaxRetries(2)
          .handle(IOException.class)
          .handleResultIf((result) -> result.equals("test"))
          .build();

      assertThrows(
          () -> Failsafe.with(rp).get(() -> {
            counter.incrementAndGet();
            throw new IOException();
          })
      );

      assertEquals(3, counter.get());
    }

  @Test
  public void shouldRetry5() {
      AtomicInteger counter = new AtomicInteger();
      RetryPolicy<Object> rp = RetryPolicy.builder()
          .withMaxRetries(2)
          .handle(IOException.class)
          .handleResultIf(Objects::isNull)
          .build();

      assertThrows(
          () -> Failsafe.with(rp).get(() -> {
            counter.incrementAndGet();
            throw new IOException();
          })
      );

      assertEquals(3, counter.get());
    }

  @Test
  public void shouldNotRetry1() {
      AtomicInteger counter = new AtomicInteger();
      RetryPolicy<Object> rp = RetryPolicy.builder()
          .handle(IOException.class)
          .withMaxRetries(2)
          .handleResult(null)
          .build();

      assertThrows(
          () -> Failsafe.with(rp).get(() -> {
            counter.incrementAndGet();
            throw new RuntimeException();
          })
      );

      assertEquals(1, counter.get());
    }

  @Test
  public void shouldNotRetry2() {
        AtomicInteger counter = new AtomicInteger();
        RetryPolicy<Object> rp = RetryPolicy.builder()
            .withMaxRetries(2)
            .handleResult(null)
            .build();

        Failsafe.with(rp).get(() -> {
          counter.incrementAndGet();
          return "test";
        });

        assertEquals(1, counter.get());
      }

  @Test
  public void shouldNotRetry3() {
        AtomicInteger counter = new AtomicInteger();
        RetryPolicy<Object> rp = RetryPolicy.builder()
            .withMaxRetries(2)
            .handleResultIf((result) -> result.equals("test"))
            .build();

        Failsafe.with(rp).get(() -> {
          counter.incrementAndGet();
          return "different string";
        });

        assertEquals(1, counter.get());
      }

  @Test
  public void shouldNotRetry4() {
        AtomicInteger counter = new AtomicInteger();
        RetryPolicy<Object> rp = RetryPolicy.builder()
            .withMaxRetries(2)
            .handleResultIf(Objects::nonNull)
            .build();

        Failsafe.with(rp).get(() -> {
          counter.incrementAndGet();
          return null;
        });

        assertEquals(1, counter.get());
      }

  @Test
  public void shouldNotRetry5() {
        AtomicInteger counter = new AtomicInteger();
        RetryPolicy<Object> rp = RetryPolicy.builder()
            .withMaxRetries(2)
            .handle(IOException.class)
            .handleResultIf(Objects::isNull)
            .build();

        assertThrows(
            () -> Failsafe.with(rp).get(() -> {
              counter.incrementAndGet();
              throw new RuntimeException();
            })
        );

        assertEquals(1, counter.get());
      }

  @Test
  public void shouldNotRetry6() {
        AtomicInteger counter = new AtomicInteger();
        RetryPolicy<Object> rp = RetryPolicy.builder()
            .withMaxRetries(2)
            .handle(RuntimeException.class)
            .handleResult(null)
            .build();

        assertThrows(
            () -> Failsafe.with(rp).get(() -> {
              counter.incrementAndGet();
              throw new IOException();
            })
        );

        assertEquals(1, counter.get());
      }

}

/*
 * Copyright 2016 the original author or authors.
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

import dev.failsafe.ExecutionContext;
import dev.failsafe.Failsafe;
import dev.failsafe.Fallback;
import dev.failsafe.RetryPolicy;
import dev.failsafe.testing.Testing;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class DelayableRetryPolicyTest extends Testing {
  static class UncheckedExpectedException extends RuntimeException {
  }

  static class DelayException extends UncheckedExpectedException {
  }

  @Test
  public void testUncheckedExceptionInDelayFunction() {
    RetryPolicy<Object> retryPolicy = RetryPolicy.builder().withDelayFn(ctx -> {
      throw new UncheckedExpectedException();
    }).build();

    // Sync
    assertThrows(() -> Failsafe.with(retryPolicy).run((ExecutionContext<Void> context) -> {
      throw new RuntimeException("try again");
    }), UncheckedExpectedException.class);

    // Async
    assertThrows(() -> Failsafe.with(retryPolicy).runAsync((ExecutionContext<Void> context) -> {
      throw new RuntimeException("try again");
    }).get(1, TimeUnit.SECONDS), ExecutionException.class, UncheckedExpectedException.class);
  }

  @Test
  public void shouldDelayOnMatchingResult() {
    AtomicInteger delays = new AtomicInteger(0);
    RetryPolicy<Object> retryPolicy = RetryPolicy.builder()
      .handleResultIf(result -> true)
      .withMaxRetries(4)
      .withDelayFnWhen(ctx -> {
        delays.incrementAndGet(); // side-effect for test purposes
        return Duration.ofNanos(1);
      }, "expected")
      .build();
    Fallback<Object> fallback = Fallback.<Object>builder(123).handleResultIf(result -> true).build();

    AtomicInteger attempts = new AtomicInteger(0);
    Object result = Failsafe.with(fallback, retryPolicy).get(() -> {
      int i = attempts.getAndIncrement();
      switch (i) {
        case 0:
        case 3:
          return "expected";
        default:
          return i;
      }
    });

    assertEquals(result, 123);
    assertEquals(attempts.get(), 5);
    assertEquals(delays.get(), 2);
  }

  @Test
  public void shouldDelayOnMatchingFailureType() {
    AtomicInteger delays = new AtomicInteger(0);
    RetryPolicy<Integer> retryPolicy = RetryPolicy.<Integer>builder()
      .handle(UncheckedExpectedException.class)
      .withMaxRetries(4)
      .withDelayFnOn(ctx -> {
        delays.incrementAndGet(); // side-effect for test purposes
        return Duration.ofNanos(1);
      }, DelayException.class)
      .build();

    AtomicInteger attempts = new AtomicInteger(0);
    int result = Failsafe.with(Fallback.of(123), retryPolicy).get(() -> {
      int i = attempts.getAndIncrement();
      switch (i) {
        case 0:
        case 2:
          throw new DelayException();
        default:
          throw new UncheckedExpectedException();
      }
    });

    assertEquals(result, 123);
    assertEquals(attempts.get(), 5);
    assertEquals(delays.get(), 2);
  }
}
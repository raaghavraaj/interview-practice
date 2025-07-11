/*
 * Copyright 2021 the original author or authors.
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

import dev.failsafe.*;
import dev.failsafe.testing.Testing;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

/**
 * Tests various Timeout policy scenarios.
 */
public class TimeoutTest extends Testing {
  /**
   * Tests a simple execution that does not timeout.
   */
  @Test
  public void shouldNotTimeout() {
    // Given
    Timeout<Object> timeout = Timeout.of(Duration.ofSeconds(1));

    // When / Then
    testGetSuccess(Failsafe.with(timeout), ctx -> {
      return "success";
    }, (f, e) -> {
      assertEquals(e.getAttemptCount(), 1);
      assertEquals(e.getExecutionCount(), 1);
    }, "success");
  }

  @Test
  public void shouldCancelTimeoutWhenExecutionComplete() {
    // TODO
  }

  /**
   * Tests that an inner timeout does not prevent outer retries from being performed when the inner Supplier is
   * blocked.
   */
  @Test
  public void testRetryTimeoutWithBlockedSupplier() {
    Stats timeoutStats = new Stats();
    Stats rpStats = new Stats();
    RetryPolicy<Object> retryPolicy = withStatsAndLogs(RetryPolicy.builder(), rpStats).build();

    Consumer<Timeout<Object>> test = timeout -> testGetSuccess(false, () -> {
      timeoutStats.reset();
      rpStats.reset();
    }, Failsafe.with(retryPolicy, timeout), ctx -> {
      if (ctx.getAttemptCount() < 2)
        Thread.sleep(100);
      return false;
    }, (f, e) -> {
      assertEquals(e.getAttemptCount(), 3);
      assertEquals(e.getExecutionCount(), 3);
      assertEquals(timeoutStats.executionCount, 3);
      assertEquals(rpStats.retryCount, 2);
    }, false);

    // Test without interrupt
    Timeout<Object> timeout = withStatsAndLogs(Timeout.builder(Duration.ofMillis(50)), timeoutStats).build();
    test.accept(timeout);

    // Test with interrupt
    timeout = withStatsAndLogs(Timeout.builder(Duration.ofMillis(50)).withInterrupt(), timeoutStats).build();
    test.accept(timeout);
  }

  /**
   * Tests that when an outer retry is scheduled any inner timeouts are cancelled. This prevents the timeout from
   * accidentally cancelling a scheduled retry that may be pending.
   */
  @Test
  public void testRetryTimeoutWithPendingRetry() {
    Stats timeoutStats = new Stats();
    RetryPolicy<Object> retryPolicy = withLogs(RetryPolicy.builder().withDelay(Duration.ofMillis(100))).build();

    Consumer<Timeout<Object>> test = timeout -> testRunFailure(() -> {
      timeoutStats.reset();
    }, Failsafe.with(retryPolicy, timeout), ctx -> {
      throw new IllegalStateException();
    }, (f, e) -> {
      assertEquals(e.getAttemptCount(), 3);
      assertEquals(e.getExecutionCount(), 3);
      assertEquals(timeoutStats.successCount, 3);
      assertEquals(timeoutStats.failureCount, 0);
    }, IllegalStateException.class);

    // Test without interrupt
    Timeout<Object> timeout = withStatsAndLogs(Timeout.builder(Duration.ofMillis(100)), timeoutStats).build();
    test.accept(timeout);

    // Test with interrupt
    timeout = withStatsAndLogs(Timeout.builder(Duration.ofMillis(100)).withInterrupt(), timeoutStats).build();
    test.accept(timeout);
  }

  /**
   * Tests that an outer timeout will cancel inner retries when the inner Supplier is blocked. The flow should be:
   * <p>
   * <br>Execution that retries a few times, blocking each time
   * <br>Timeout
   */
  @Test
  public void testTimeoutRetryWithBlockedSupplier() {
    Stats timeoutStats = new Stats();
    RetryPolicy<Object> retryPolicy = RetryPolicy.ofDefaults();

    Consumer<Timeout<Object>> test = timeout -> testRunFailure(() -> {
      timeoutStats.reset();
    }, Failsafe.with(timeout, retryPolicy), ctx -> {
      System.out.println("Executing");
      Thread.sleep(60);
      throw new Exception();
    }, (f, e) -> {
      assertEquals(e.getAttemptCount(), 3);
      assertEquals(e.getExecutionCount(), 3);
      assertEquals(timeoutStats.failureCount, 1);
    }, TimeoutExceededException.class);

    // Test without interrupt
    Timeout<Object> timeout = withStatsAndLogs(Timeout.builder(Duration.ofMillis(150)), timeoutStats).build();
    test.accept(timeout);

    // Test with interrupt
    timeout = withStatsAndLogs(Timeout.builder(Duration.ofMillis(150)).withInterrupt(), timeoutStats).build();
    test.accept(timeout);
  }

  /**
   * Tests that an outer timeout will cancel inner retries when an inner retry is pending. The flow should be:
   * <p>
   * <br>Execution
   * <br>Retry sleep/scheduled that blocks
   * <br>Timeout
   */
  @Test
  public void testTimeoutRetryWithPendingRetry() {
    Stats timeoutStats = new Stats();
    Stats rpStats = new Stats();
    RetryPolicy<Object> retryPolicy = withStatsAndLogs(RetryPolicy.builder().withDelay(Duration.ofMillis(1000)),
      rpStats).build();

    Consumer<Timeout<Object>> test = timeout -> testRunFailure(false, () -> {
      timeoutStats.reset();
      rpStats.reset();
    }, Failsafe.with(timeout).compose(retryPolicy), ctx -> {
      System.out.println("Executing");
      throw new Exception();
    }, (f, e) -> {
      assertEquals(e.getAttemptCount(), 1);
      assertEquals(e.getExecutionCount(), 1);
      assertEquals(timeoutStats.failureCount, 1);
      assertEquals(rpStats.failedAttemptCount, 1);
    }, TimeoutExceededException.class);

    // Test without interrupt
    Timeout<Object> timeout = withStatsAndLogs(Timeout.builder(Duration.ofMillis(100)), timeoutStats).build();
    test.accept(timeout);

    // Test with interrupt
    timeout = withStatsAndLogs(Timeout.builder(Duration.ofMillis(100)).withInterrupt(), timeoutStats).build();
    test.accept(timeout);
  }

  /**
   * Tests an inner timeout that fires while the supplier is blocked.
   */
  @Test
  public void testFallbackTimeoutWithBlockedSupplier() {
    Stats timeoutStats = new Stats();
    Stats fbStats = new Stats();
    Fallback<Object> fallback = withStatsAndLogs(Fallback.builder(() -> {
      System.out.println("Falling back");
      throw new IllegalStateException();
    }), fbStats).build();

    Consumer<Timeout<Object>> test = timeout -> testRunFailure(false, () -> {
      timeoutStats.reset();
      fbStats.reset();
    }, Failsafe.with(fallback).compose(timeout), ctx -> {
      System.out.println("Executing");
      Thread.sleep(100);
      throw new Exception();
    }, (f, e) -> {
      assertEquals(e.getAttemptCount(), 1);
      assertEquals(e.getExecutionCount(), 1);
      assertEquals(timeoutStats.failureCount, 1);
      assertEquals(fbStats.executionCount, 1);
    }, IllegalStateException.class);

    // Test without interrupt
    Timeout<Object> timeout = withStatsAndLogs(Timeout.builder(Duration.ofMillis(10)), timeoutStats).build();
    test.accept(timeout);

    // Test with interrupt
    timeout = withStatsAndLogs(Timeout.builder(Duration.ofMillis(10)).withInterrupt(), timeoutStats).build();
    test.accept(timeout);
  }

  /**
   * Tests that an inner timeout will not interrupt an outer fallback. The inner timeout is never triggered since the
   * supplier completes immediately.
   */
  @Test
  public void testFallbackTimeout() {
    Stats timeoutStats = new Stats();
    Stats fbStats = new Stats();
    Fallback<Object> fallback = withStatsAndLogs(Fallback.builder(() -> {
      System.out.println("Falling back");
      throw new IllegalStateException();
    }), fbStats).build();

    Consumer<Timeout<Object>> test = timeout -> testRunFailure(() -> {
      timeoutStats.reset();
      fbStats.reset();
    }, Failsafe.with(fallback).compose(timeout), ctx -> {
      System.out.println("Executing");
      throw new Exception();
    }, (f, e) -> {
      assertEquals(e.getAttemptCount(), 1);
      assertEquals(e.getExecutionCount(), 1);
      assertEquals(timeoutStats.failureCount, 0);
      assertEquals(fbStats.executionCount, 1);
    }, IllegalStateException.class);

    // Test without interrupt
    Timeout<Object> timeout = withStatsAndLogs(Timeout.builder(Duration.ofMillis(100)), timeoutStats).build();
    test.accept(timeout);

    // Test with interrupt
    timeout = withStatsAndLogs(Timeout.builder(Duration.ofMillis(100)).withInterrupt(), timeoutStats).build();
    test.accept(timeout);
  }

  /**
   * Tests that an outer timeout will interrupt an inner supplier that is blocked, skipping the inner fallback.
   */
  @Test
  public void testTimeoutFallbackWithBlockedSupplier() {
    Stats timeoutStats = new Stats();
    Stats fbStats = new Stats();
    Fallback<Object> fallback = withStatsAndLogs(Fallback.builder(() -> {
      System.out.println("Falling back");
      throw new IllegalStateException();
    }), fbStats).build();

    Consumer<Timeout<Object>> test = timeout -> testRunFailure(false, () -> {
      timeoutStats.reset();
      fbStats.reset();
    }, Failsafe.with(timeout).compose(fallback), ctx -> {
      System.out.println("Executing");
      Thread.sleep(100);
      throw new Exception();
    }, (f, e) -> {
      assertEquals(e.getAttemptCount(), 1);
      assertEquals(e.getExecutionCount(), 1);
      assertEquals(timeoutStats.failureCount, 1);
      assertEquals(fbStats.executionCount, 0);
    }, TimeoutExceededException.class);

    // Test without interrupt
    Timeout<Object> timeout = withStatsAndLogs(Timeout.builder(Duration.ofMillis(1)), timeoutStats).build();
    test.accept(timeout);

    // Test with interrupt
    timeout = withStatsAndLogs(Timeout.builder(Duration.ofMillis(1)).withInterrupt(), timeoutStats).build();
    test.accept(timeout);
  }

  /**
   * Tests that an outer timeout will interrupt an inner fallback that is blocked.
   */
  @Test
  public void testTimeoutFallbackWithBlockedFallback() {
    Stats timeoutStats = new Stats();
    Stats fbStats = new Stats();
    Fallback<Object> fallback = withStatsAndLogs(Fallback.builder(() -> {
      System.out.println("Falling back");
      Thread.sleep(200);
      throw new IllegalStateException();
    }), fbStats).build();

    Consumer<Timeout<Object>> test = timeout -> testRunFailure(false, () -> {
      timeoutStats.reset();
      fbStats.reset();
    }, Failsafe.with(timeout).compose(fallback), ctx -> {
      System.out.println("Executing");
      throw new Exception();
    }, (f, e) -> {
      assertEquals(e.getAttemptCount(), 1);
      assertEquals(e.getExecutionCount(), 1);
      assertEquals(timeoutStats.failureCount, 1);
      assertEquals(fbStats.executionCount, 1);
    }, TimeoutExceededException.class);

    // Test without interrupt
    Timeout<Object> timeout = withStatsAndLogs(Timeout.builder(Duration.ofMillis(100)), timeoutStats).build();
    test.accept(timeout);

    // Test with interrupt
    timeout = withStatsAndLogs(Timeout.builder(Duration.ofMillis(100)).withInterrupt(), timeoutStats).build();
    test.accept(timeout);
  }
}

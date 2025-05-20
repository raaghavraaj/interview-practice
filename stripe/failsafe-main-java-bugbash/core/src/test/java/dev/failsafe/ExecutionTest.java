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
package dev.failsafe;


import org.junit.Test;

import java.net.ConnectException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static dev.failsafe.testing.Testing.failures;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Jonathan Halterman
 */
public class ExecutionTest {
  ConnectException e = new ConnectException();

  @Test
  public void testRetryForResult() {
    // Given rpRetry for null
    Execution<Object> exec = Execution.of(RetryPolicy.builder().handleResult(null).build());

    // When / Then
    exec.recordResult(null);
    assertFalse(exec.isComplete());
    exec.recordResult(null);
    assertFalse(exec.isComplete());
    exec.recordResult(1);
    assertTrue(exec.isComplete());

    // Then
    assertEquals(3, exec.getAttemptCount());
    assertEquals(3, exec.getExecutionCount());
    assertTrue(exec.isComplete());
    assertEquals(1, exec.getLastResult());
    assertNull(exec.getLastException());

    // Given 2 max retries
    exec = Execution.of(RetryPolicy.builder().handleResult(null).build());

    // When / Then
    exec.recordResult(null);
    assertFalse(exec.isComplete());
    exec.recordResult(null);
    assertFalse(exec.isComplete());
    exec.recordResult(null);
    assertTrue(exec.isComplete());

    // Then
    assertEquals(3, exec.getAttemptCount());
    assertEquals(3, exec.getExecutionCount());
    assertTrue(exec.isComplete());
    assertNull(exec.getLastResult());
    assertNull(exec.getLastException());
  }

  @Test
  public void testRetryForThrowable() {
    // Given rpRetry on IllegalArgumentException
    Execution<Object> exec = Execution.of(RetryPolicy.builder().handle(IllegalArgumentException.class).build());

    // When / Then
    exec.recordException(new IllegalArgumentException());
    assertFalse(exec.isComplete());
    exec.recordException(e);
    assertTrue(exec.isComplete());

    // Then
    assertEquals(2, exec.getAttemptCount());
    assertEquals(2, exec.getExecutionCount());
    assertTrue(exec.isComplete());
    assertNull(exec.getLastResult());
    assertEquals(e, exec.getLastException());

    // Given 2 max retries
    exec = Execution.of(RetryPolicy.ofDefaults());

    // When / Then
    exec.recordException(e);
    assertFalse(exec.isComplete());
    exec.recordException(e);
    assertFalse(exec.isComplete());
    exec.recordException(e);
    assertTrue(exec.isComplete());

    // Then
    assertEquals(3, exec.getAttemptCount());
    assertEquals(3, exec.getExecutionCount());
    assertTrue(exec.isComplete());
    assertNull(exec.getLastResult());
    assertEquals(e, exec.getLastException());
  }

  @Test
  public void testRetryForResultAndThrowable() {
    // Given rpRetry for null
    Execution<Object> exec = Execution.of(RetryPolicy.builder().withMaxAttempts(10).handleResult(null).build());

    // When / Then
    exec.recordResult(null);
    assertFalse(exec.isComplete());
    exec.record(null, null);
    assertFalse(exec.isComplete());
    exec.record(1, new IllegalArgumentException());
    assertFalse(exec.isComplete());
    exec.record(1, null);
    assertTrue(exec.isComplete());

    // Then
    assertEquals(4, exec.getAttemptCount());
    assertEquals(4, exec.getExecutionCount());
    assertTrue(exec.isComplete());

    // Given 2 max retries
    exec = Execution.of(RetryPolicy.builder().handleResult(null).build());

    // When / Then
    exec.recordResult(null);
    assertFalse(exec.isComplete());
    exec.record(null, e);
    assertFalse(exec.isComplete());
    exec.record(null, e);
    assertTrue(exec.isComplete());

    // Then
    assertEquals(3, exec.getAttemptCount());
    assertEquals(3, exec.getExecutionCount());
    assertTrue(exec.isComplete());
  }

  @Test
  public void testGetAttemptCount() {
    Execution<Object> exec = Execution.of(RetryPolicy.ofDefaults());
    exec.recordException(e);
    exec.recordException(e);
    assertEquals(2, exec.getAttemptCount());
    assertEquals(2, exec.getExecutionCount());
  }

  @Test
  public void testGetElapsedMillis() throws Throwable {
    Execution<Object> exec = Execution.of(RetryPolicy.ofDefaults());
    assertTrue(exec.getElapsedTime().toMillis() < 100);
    Thread.sleep(150);
    assertTrue(exec.getElapsedTime().toMillis() > 100);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testIsComplete() {
    List<Object> list = mock(List.class);
    when(list.size()).thenThrow(failures(2, new IllegalStateException())).thenReturn(5);

    RetryPolicy<Object> retryPolicy = RetryPolicy.builder().handle(IllegalStateException.class).build();
    Execution<Object> exec = Execution.of(retryPolicy);

    while (!exec.isComplete()) {
      try {
        exec.recordResult(list.size());
      } catch (IllegalStateException e) {
        exec.recordException(e);
      }
    }

    assertEquals(5, exec.getLastResult());
    assertEquals(3, exec.getAttemptCount());
    assertEquals(3, exec.getExecutionCount());
  }

  @Test
  public void shouldNotAllowNegativeDelaysAndMaxDuration() {
    Execution<Object> exec = Execution.of(
        RetryPolicy.builder().withMaxAttempts(10)
            .withMaxDuration(Duration.ofSeconds(1))
            .withDelayFn((x) -> Duration.ofMillis(-1))
            .build());
    assertEquals(exec.getDelay().toNanos(), 0);
    exec.recordException(e);
    assertEquals(exec.getDelay().toNanos(), 0);
  }

  @Test
  public void shouldNotAllowForNegativeDelaysToPropagate() {
    Execution<Object> exec = Execution.of(
        RetryPolicy.builder().withMaxAttempts(10)
            .withDelayFn((x) -> Duration.ofMillis(-1))
            .build());
    assertEquals(0, exec.getDelay().toNanos());
    exec.recordException(e);
    assertTrue(exec.getDelay().toNanos() >= 0);
  }

  @Test
  public void shouldAdjustDelayForBackoff() {
    Execution<Object> exec = Execution.of(
      RetryPolicy.builder().withMaxAttempts(10).withBackoff(Duration.ofNanos(1), Duration.ofNanos(10)).build());
    assertEquals(0, exec.getDelay().toNanos());
    exec.recordException(e);
    assertEquals(1, exec.getDelay().toNanos());
    exec.recordException(e);
    assertEquals(2, exec.getDelay().toNanos());
    exec.recordException(e);
    assertEquals(4, exec.getDelay().toNanos());
    exec.recordException(e);
    assertEquals(8, exec.getDelay().toNanos());
    exec.recordException(e);
    assertEquals(10, exec.getDelay().toNanos());
    exec.recordException(e);
    assertEquals(10, exec.getDelay().toNanos());
  }

  @Test
  public void shouldAdjustDelayForComputedDelay() {
    Execution<Object> exec = Execution.of(RetryPolicy.builder()
      .withMaxAttempts(10)
      .withDelayFn(ctx -> Duration.ofNanos(ctx.getAttemptCount() * 2))
      .build());
    assertEquals(0, exec.getDelay().toNanos());
    exec.recordException(e);
    assertEquals(2, exec.getDelay().toNanos());
    exec.recordException(e);
    assertEquals(4, exec.getDelay().toNanos());
    exec.recordException(e);
    assertEquals(6, exec.getDelay().toNanos());
    exec.recordException(e);
    assertEquals(8, exec.getDelay().toNanos());
  }

  @Test
  public void shouldFallbackDelayFromNegativeComputedToFixedDelay() {
    Execution<Object> exec = Execution.of(RetryPolicy.builder()
      .withMaxAttempts(10)
      .withDelay(Duration.ofNanos(5))
      .withDelayFn(ctx -> Duration.ofNanos(ctx.getAttemptCount() % 2 == 0 ? ctx.getAttemptCount() * 2 : -1))
      .build());
    assertEquals(0, exec.getDelay().toNanos());
    exec.recordException(e);
    assertEquals(5, exec.getDelay().toNanos());
    exec.recordException(e);
    assertEquals(4, exec.getDelay().toNanos());
    exec.recordException(e);
    assertEquals(5, exec.getDelay().toNanos());
    exec.recordException(e);
    assertEquals(8, exec.getDelay().toNanos());
    exec.recordException(e);
    assertEquals(5, exec.getDelay().toNanos());
    exec.recordException(e);
    assertEquals(12, exec.getDelay().toNanos());
  }

  @Test
  public void shouldFallbackDelayFromNegativeComputedToBackoffDelay() {
    Execution<Object> exec = Execution.of(RetryPolicy.builder()
      .withMaxAttempts(10)
      .withBackoff(Duration.ofNanos(1), Duration.ofNanos(10))
      .withDelayFn(ctx -> Duration.ofNanos(ctx.getAttemptCount() % 2 == 0 ? ctx.getAttemptCount() * 2 : -1))
      .build());
    assertEquals(0, exec.getDelay().toNanos());
    exec.recordException(e);
    assertEquals(1, exec.getDelay().toNanos());
    exec.recordException(e);
    assertEquals(4, exec.getDelay().toNanos());
    exec.recordException(e);
    assertEquals(2, exec.getDelay().toNanos());
    exec.recordException(e);
    assertEquals(8, exec.getDelay().toNanos());
    exec.recordException(e);
    assertEquals(4, exec.getDelay().toNanos());
    exec.recordException(e);
    assertEquals(12, exec.getDelay().toNanos());
    exec.recordException(e);
    assertEquals(8, exec.getDelay().toNanos());
  }

  @Test
  public void shouldAdjustDelayForMaxDuration() throws Throwable {
    Execution<Object> exec = Execution.of(
      RetryPolicy.builder().withDelay(Duration.ofMillis(49)).withMaxDuration(Duration.ofMillis(50)).build());
    Thread.sleep(10);
    exec.recordException(e);
    assertFalse(exec.isComplete());
    assertTrue(exec.getDelay().toNanos() < TimeUnit.MILLISECONDS.toNanos(50) && exec.getDelay().toNanos() > 0);
  }

  @Test
  public void shouldSupportMaxDuration() throws Exception {
    Execution<Object> exec = Execution.of(RetryPolicy.builder().withMaxDuration(Duration.ofMillis(100)).build());
    exec.recordException(e);
    assertFalse(exec.isComplete());
    exec.recordException(e);
    assertFalse(exec.isComplete());
    Thread.sleep(105);
    exec.recordException(e);
    assertTrue(exec.isComplete());
  }

  @Test
  public void shouldSupportMaxRetries() {
    Execution<Object> exec = Execution.of(RetryPolicy.builder().withMaxRetries(3).build());
    exec.recordException(e);
    assertFalse(exec.isComplete());
    exec.recordException(e);
    assertFalse(exec.isComplete());
    exec.recordException(e);
    assertFalse(exec.isComplete());
    exec.recordException(e);
    assertTrue(exec.isComplete());
  }

  @Test
  public void shouldGetDelayMillis() throws Throwable {
    Execution<Object> exec = Execution.of(RetryPolicy.builder()
      .withDelay(Duration.ofMillis(100))
      .withMaxDuration(Duration.ofMillis(101))
      .handleResult(null)
      .build());
    assertEquals(0, exec.getDelay().toMillis());
    exec.recordResult(null);
    assertTrue(exec.getDelay().toMillis() <= 100);
    Thread.sleep(150);
    exec.recordResult(null);
    assertTrue(exec.isComplete());
    assertEquals(0, exec.getDelay().toMillis());
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowOnMultipleCompletes() {
    Execution<Object> exec = Execution.of(RetryPolicy.ofDefaults());
    exec.complete();
    exec.complete();
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowOnCanRetryWhenAlreadyComplete() {
    Execution<Object> exec = Execution.of(RetryPolicy.ofDefaults());
    exec.complete();
    exec.recordException(e);
  }
}

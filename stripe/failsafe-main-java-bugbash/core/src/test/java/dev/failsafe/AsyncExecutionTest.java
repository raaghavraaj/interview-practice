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

import dev.failsafe.spi.*;
import dev.failsafe.testing.Testing;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AsyncExecutionTest extends Testing {
  Function<AsyncExecutionInternal<Object>, CompletableFuture<ExecutionResult<Object>>> innerFn = Functions.getPromise(
    ctx -> null, null);
  ConnectException e = new ConnectException();
  AsyncExecutionInternal<Object> exec;
  FailsafeFuture<Object> future;
  Callable<Object> callable;
  Scheduler scheduler;

  @Before
  @SuppressWarnings("unchecked")
  public void beforeMethod() {
    scheduler = mock(Scheduler.class);
    when(scheduler.schedule(any(Callable.class), anyLong(), any(TimeUnit.class))).thenReturn(
      new DefaultScheduledFuture<>());
    future = mock(FailsafeFuture.class);
    callable = mock(Callable.class);
  }

  @Test
  public void testCompleteForNoResult() {
    // Given
    exec = new AsyncExecutionImpl<>(Arrays.asList(RetryPolicy.ofDefaults()), scheduler, future, true, innerFn);

    // When
    exec.preExecute();
    exec.complete();

    // Then
    assertEquals(exec.getAttemptCount(), 1);
    assertEquals(exec.getExecutionCount(), 1);
    assertTrue(exec.isComplete());
    assertNull(exec.getLastResult());
    assertNull(exec.getLastException());
    verify(future).completeResult(ExecutionResult.none());
  }

  @Test
  public void testRetryForResult() {
    // Given retry for null
    exec = new AsyncExecutionImpl<>(Arrays.asList(RetryPolicy.builder().handleResult(null).build()), scheduler, future,
      true, innerFn);

    // When / Then
    exec.preExecute();
    exec.recordResult(null);
    assertFalse(exec.isComplete());
    exec = exec.copy();
    exec.preExecute();
    exec.recordResult(null);
    assertFalse(exec.isComplete());
    exec = exec.copy();
    exec.preExecute();
    exec.recordResult(1);
    assertTrue(exec.isComplete());

    // Then
    assertEquals(exec.getAttemptCount(), 3);
    assertEquals(exec.getExecutionCount(), 3);
    assertTrue(exec.isComplete());
    assertEquals(exec.getLastResult(), 1);
    assertNull(exec.getLastException());
    verifyScheduler(2);
    verify(future).completeResult(ExecutionResult.success(1));
  }

  @Test
  public void testRetryForThrowable() {
    // Given retry on IllegalArgumentException
    exec = new AsyncExecutionImpl<>(Arrays.asList(RetryPolicy.builder().handle(IllegalArgumentException.class).build()),
      scheduler, future, true, innerFn);

    // When / Then
    exec.preExecute();
    exec.recordException(new IllegalArgumentException());
    assertFalse(exec.isComplete());
    exec = exec.copy();
    exec.preExecute();
    exec.recordException(e);
    assertTrue(exec.isComplete());

    // Then
    assertEquals(exec.getAttemptCount(), 2);
    assertEquals(exec.getExecutionCount(), 2);
    assertTrue(exec.isComplete());
    assertNull(exec.getLastResult());
    assertEquals(exec.getLastException(), e);
    verifyScheduler(1);
    verify(future).completeResult(ExecutionResult.exception(e));
  }

  @Test
  public void testRetryForResultAndThrowable() {
    // Given retry for null
    exec = new AsyncExecutionImpl<>(Arrays.asList(RetryPolicy.builder().withMaxAttempts(10).handleResult(null).build()),
      scheduler, future, true, innerFn);

    // When / Then
    exec.preExecute();
    exec.recordResult(null);
    assertFalse(exec.isComplete());
    exec = exec.copy();
    exec.preExecute();
    exec.record(null, null);
    assertFalse(exec.isComplete());
    exec = exec.copy();
    exec.preExecute();
    exec.record(1, new IllegalArgumentException());
    assertFalse(exec.isComplete());
    exec = exec.copy();
    exec.preExecute();
    exec.record(1, null);
    assertTrue(exec.isComplete());

    // Then
    assertEquals(exec.getAttemptCount(), 4);
    assertEquals(exec.getExecutionCount(), 4);
    assertTrue(exec.isComplete());
    assertEquals(exec.getLastResult(), 1);
    assertNull(exec.getLastException());
    verifyScheduler(3);
    verify(future).completeResult(ExecutionResult.success(1));
  }

  @Test
  public void testGetAttemptCount() {
    // Given
    exec = new AsyncExecutionImpl<>(Arrays.asList(RetryPolicy.ofDefaults()), scheduler, future, true, innerFn);

    // When
    exec.preExecute();
    exec.recordException(e);
    exec = exec.copy();
    exec.preExecute();
    exec.recordException(e);

    // Then
    assertEquals(exec.getAttemptCount(), 2);
    assertEquals(exec.getExecutionCount(), 2);
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowOnRetryWhenAlreadyComplete() {
    exec = new AsyncExecutionImpl<>(Arrays.asList(RetryPolicy.ofDefaults()), scheduler, future, true, innerFn);
    exec.complete();
    exec.preExecute();
    exec.recordException(e);
  }

  @Test
  public void testCompleteOrRetry() {
    // Given retry on IllegalArgumentException
    exec = new AsyncExecutionImpl<>(Arrays.asList(RetryPolicy.ofDefaults()), scheduler, future, true, innerFn);

    // When / Then
    exec.preExecute();
    exec.record(null, e);
    assertFalse(exec.isComplete());
    exec = exec.copy();
    exec.preExecute();
    exec.record(null, null);

    // Then
    assertEquals(exec.getAttemptCount(), 2);
    assertEquals(exec.getExecutionCount(), 2);
    assertTrue(exec.isComplete());
    assertNull(exec.getLastResult());
    assertNull(exec.getLastException());
    verifyScheduler(1);
    verify(future).completeResult(ExecutionResult.none());
  }

  private void verifyScheduler(int executions) {
    verify(scheduler, times(executions)).schedule(any(Callable.class), any(Long.class), any(TimeUnit.class));
  }
}
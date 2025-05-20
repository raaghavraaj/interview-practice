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
package dev.failsafe.internal;

import dev.failsafe.CircuitBreaker;
import dev.failsafe.CircuitBreaker.State;
import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.*;

public class OpenStateTest {
  @Test
  public void testTryAcquirePermit() throws Throwable {
    // Given
    CircuitBreakerImpl<Object> breaker = (CircuitBreakerImpl<Object>) CircuitBreaker.builder()
      .withDelay(Duration.ofMillis(100))
      .build();
    breaker.open();
    OpenState<Object> state = new OpenState<>(breaker, new ClosedState<>(breaker), breaker.getConfig().getDelay());
    assertTrue(breaker.isOpen());
    assertFalse(state.tryAcquirePermit());

    // When
    Thread.sleep(110);

    // Then
    assertTrue(state.tryAcquirePermit());
    assertEquals(breaker.getState(), State.HALF_OPEN);
  }

  @Test
  public void testRemainingDelay() throws Throwable {
    // Given
    CircuitBreakerImpl<Object> breaker = (CircuitBreakerImpl<Object>) CircuitBreaker.builder()
      .withDelay(Duration.ofSeconds(1))
      .build();
    OpenState<Object> state = new OpenState<>(breaker, new ClosedState<>(breaker), breaker.getConfig().getDelay());

    // When / Then
    long remainingDelayMillis = state.getRemainingDelay().toMillis();
    assertTrue(remainingDelayMillis < 1000);
    assertTrue(remainingDelayMillis > 0);

    Thread.sleep(110);
    remainingDelayMillis = state.getRemainingDelay().toMillis();
    assertTrue(remainingDelayMillis < 900);
    assertTrue(remainingDelayMillis > 0);
  }

  @Test
  public void testNoRemainingDelay() throws Throwable {
    // Given
    CircuitBreakerImpl<Object> breaker = (CircuitBreakerImpl<Object>) CircuitBreaker.builder()
      .withDelay(Duration.ofMillis(10))
      .build();
    assertEquals(breaker.getRemainingDelay(), Duration.ZERO);

    // When
    OpenState<Object> state = new OpenState<>(breaker, new ClosedState<>(breaker), breaker.getConfig().getDelay());
    Thread.sleep(50);

    // Then
    assertEquals(state.getRemainingDelay().toMillis(), 0);
  }
}

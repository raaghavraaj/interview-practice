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

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import dev.failsafe.testing.Testing;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

public class RetryPolicyJitterTest extends Testing {
  @Test
  public void whenJitterSpecifiedRetriesWithJitter() {
    AtomicInteger counter = new AtomicInteger();

    //I expect this to have a jitter of +/- 10ms
    RetryPolicy<Object> rp = RetryPolicy.builder()
        .handle(IllegalStateException.class)
        .withMaxRetries(2)
        .withJitter(Duration.ofMillis(10))
        .withDelay(Duration.ofMillis(10))
        .onRetryScheduled(event -> assertTrue(event.getDelay().toMillis() >= 10 && event.getDelay().toMillis() <= 20))
        .build();

    Assert.assertThrows(
        IllegalStateException.class,
        () -> Failsafe.with(rp).get(() -> {
          counter.incrementAndGet();
          throw new IllegalStateException();
        })
    );

    assertEquals(counter.get(), 3);
  }

  @Test
  public void whenZeroJitterFactorFallsBackToFixed() {
    AtomicInteger counter = new AtomicInteger();

    //I expect this to fall-back on a fixed backoff with no jitter
    RetryPolicy<Object> rp = RetryPolicy.builder()
        .handle(IllegalStateException.class)
        .withMaxRetries(2)
        .withJitter(0.0)
        .withDelay(Duration.ofMillis(10))
        .onRetryScheduled(event -> assertEquals(event.getDelay().toMillis(), 10))
        .build();

    Assert.assertThrows(
        IllegalStateException.class,
        () -> Failsafe.with(rp).get(() -> {
          counter.incrementAndGet();
          throw new IllegalStateException();
        })
    );

    assertEquals(counter.get(), 3);
  }

  @Test
  public void whenJitterSpecifiedRetriesWithJitterFactor() {
    AtomicInteger counter = new AtomicInteger();

    //I expect this to have a jitter of 2 * 10ms = +/- 10ms
    RetryPolicy<Object> rp = RetryPolicy.builder()
        .handle(IllegalStateException.class)
        .withMaxRetries(2)
        .withJitter(1.0)
        .withDelay(Duration.ofMillis(10))
        .onRetryScheduled(event -> assertTrue(event.getDelay().toMillis() >= 10 && event.getDelay().toMillis() <= 30))
        .build();

    Assert.assertThrows(
        IllegalStateException.class,
        () -> Failsafe.with(rp).get(() -> {
          counter.incrementAndGet();
          throw new IllegalStateException();
        })
    );

    assertEquals(counter.get(), 3);
  }

  @Test
  public void testJitterFactor() {
    AtomicLong delay = new AtomicLong(0);

    //I expect this to have a jitter of .001 * 1000ms = +/- 1ms
    RetryPolicy<Object> rp = RetryPolicy.builder()
        .handle(IllegalStateException.class)
        .withMaxRetries(1)
        .withJitter(Duration.ofMillis(100)) //Jitter of 100 ms
        .withJitter(.001) //JitterFactor of .001x duration (1ms)
        .withDelay(Duration.ofMillis(1000))
        .onRetryScheduled(event -> delay.set(event.getDelay().toMillis()))
        .build();

    Assert.assertThrows(
        IllegalStateException.class,
        () -> Failsafe.with(rp).get(() -> {
          throw new IllegalStateException();
        })
    );

    assertNotEquals(delay.get(), 0L);
    assertTrue(delay.get() >= 999L);
    assertTrue(delay.get() <= 1001L);
  }

  @Test
  public void testFixedJitter() {
    AtomicLong delay = new AtomicLong(0);

    //I expect this to have a jitter of +/- 100ms
    RetryPolicy<Object> rp = RetryPolicy.builder()
        .handle(IllegalStateException.class)
        .withMaxRetries(1)
        .withJitter(0.1) //JitterFactor of .1x duration
        .withJitter(Duration.ofMillis(10)) //Jitter of 100 ms
        .withDelay(Duration.ofMillis(100))
        .onRetryScheduled(event -> delay.set(event.getDelay().toMillis()))
        .build();

    Assert.assertThrows(
        IllegalStateException.class,
        () -> Failsafe.with(rp).get(() -> {
          throw new IllegalStateException();
        })
    );

    assertNotEquals(delay.get(), 0L);
    assertTrue(delay.get() >= 90L);
    assertTrue(delay.get() <= 110L);
  }
}

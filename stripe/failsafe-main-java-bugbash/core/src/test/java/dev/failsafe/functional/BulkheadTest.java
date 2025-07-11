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

import dev.failsafe.Bulkhead;
import dev.failsafe.BulkheadFullException;
import dev.failsafe.Failsafe;
import dev.failsafe.testing.Testing;
import org.junit.Test;

import java.time.Duration;

/**
 * Tests various Bulkhead scenarios.
 */
public class BulkheadTest extends Testing {
  @Test
  public void testPermitAcquiredAfterWait() {
    // Given
    Bulkhead<Object> bulkhead = Bulkhead.builder(2).withMaxWaitTime(Duration.ofSeconds(1)).build();

    // When / Then
    testGetSuccess(() -> {
      bulkhead.tryAcquirePermit();
      bulkhead.tryAcquirePermit(); // bulkhead should be full

      runInThread(() -> {
        Thread.sleep(200);
        bulkhead.releasePermit(); // bulkhead should not be full
      });
    }, Failsafe.with(bulkhead), ctx -> {
      return "test";
    }, "test");
  }

  @Test
  public void shouldThrowBulkheadFullExceptionAfterPermitsExceeded() {
    // Given
    Bulkhead<Object> bulkhead = Bulkhead.of(2);
    bulkhead.tryAcquirePermit();
    bulkhead.tryAcquirePermit(); // bulkhead should be full

    // When / Then
    testRunFailure(Failsafe.with(bulkhead), ctx -> {
    }, BulkheadFullException.class);
  }

  /**
   * Asserts that an exceeded maxWaitTime causes BulkheadFullException.
   */
  @Test
  public void testMaxWaitTimeExceeded() {
    // Given
    Bulkhead<Object> bulkhead = Bulkhead.builder(2).withMaxWaitTime(Duration.ofMillis(20)).build();
    bulkhead.tryAcquirePermit();
    bulkhead.tryAcquirePermit(); // bulkhead should be full

    // When / Then
    testRunFailure(Failsafe.with(bulkhead), ctx -> {
    }, BulkheadFullException.class);
  }
}

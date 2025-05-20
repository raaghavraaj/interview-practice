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
package dev.failsafe.internal.util;

import org.junit.Test;

import java.time.Duration;

import static dev.failsafe.internal.util.Durations.*;
import static org.junit.Assert.assertEquals;

public class DurationsTest {
  @Test
  public void testOfSafeNanos() {
    assertEquals(ofSafeNanos(Duration.ofSeconds(MAX_SECONDS_PER_LONG)), MAX_SAFE_NANOS_DURATION);
    assertEquals(ofSafeNanos(Duration.ofSeconds(MAX_SECONDS_PER_LONG + 1)), MAX_SAFE_NANOS_DURATION);
    assertEquals(ofSafeNanos(Duration.ofSeconds(Long.MAX_VALUE)), MAX_SAFE_NANOS_DURATION);
    Duration safeDuration = Duration.ofSeconds(MAX_SECONDS_PER_LONG - 1000);
    assertEquals(ofSafeNanos(safeDuration), safeDuration);
  }
}

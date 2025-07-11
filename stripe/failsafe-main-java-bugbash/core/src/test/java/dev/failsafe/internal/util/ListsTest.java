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
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class ListsTest {
  @Test
  public void testOf() {
    assertEquals(Lists.of("a", new String[] { "b", "c" }), Arrays.asList("a", "b", "c"));
  }
}

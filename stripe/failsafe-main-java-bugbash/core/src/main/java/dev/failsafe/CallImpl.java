/*
 * Copyright 2022 the original author or authors.
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

/**
 * A call implementation that delegates to an execution.
 *
 * @param <R> result type
 * @author Jonathan Halterman
 */
class CallImpl<R> implements Call<R> {
  private volatile SyncExecutionImpl<R> execution;

  void setExecution(SyncExecutionImpl<R> execution) {
    this.execution = execution;
  }

  @Override
  public R execute() {
    return execution.executeSync();
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    boolean result = execution.cancel();
    if (mayInterruptIfRunning)
      execution.interrupt();
    return result;
  }

  @Override
  public boolean isCancelled() {
    return execution.isCancelled();
  }
}

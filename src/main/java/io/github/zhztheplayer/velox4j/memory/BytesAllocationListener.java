/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package io.github.zhztheplayer.velox4j.memory;

import java.util.concurrent.atomic.AtomicLong;

/** An allocation listener that accumulates the allocated bytes with an internal counter. */
public class BytesAllocationListener implements AllocationListener {
  private final AtomicLong currentBytes = new AtomicLong(0);
  private final AtomicLong peakBytes = new AtomicLong(0);

  public BytesAllocationListener() {}

  @Override
  public void allocationChanged(long diff) {
    long current = currentBytes.addAndGet(diff);
    peakBytes.getAndUpdate(prevPeak -> Math.max(prevPeak, current));
  }

  public long currentBytes() {
    return currentBytes.get();
  }

  public long peakBytes() {
    return peakBytes.get();
  }
}

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
package io.github.zhztheplayer.velox4j.connector;

import io.github.zhztheplayer.velox4j.data.RowVector;
import io.github.zhztheplayer.velox4j.iterator.DownIterator;
import io.github.zhztheplayer.velox4j.jni.JniApi;
import io.github.zhztheplayer.velox4j.jni.StaticJniApi;

/** A factory for creating {@link ExternalStream} instances. */
public class ExternalStreams {
  private final JniApi jniApi;

  public ExternalStreams(JniApi jniApi) {
    this.jniApi = jniApi;
  }

  public ExternalStream bind(DownIterator itr) {
    return jniApi.createExternalStreamFromDownIterator(itr);
  }

  /**
   * Creates a {@link BlockingQueue} that is backed by a C++ native blocking queue instance. The
   * blocking queue resulted is at the same time an ExternalStream instance that can be read by a
   * Velox scan operator.
   */
  public BlockingQueue newBlockingQueue() {
    return jniApi.createBlockingQueue();
  }

  public static class GenericExternalStream implements ExternalStream {
    private final long id;

    public GenericExternalStream(long id) {
      this.id = id;
    }

    @Override
    public long id() {
      return id;
    }
  }

  public static class BlockingQueue implements ExternalStream {
    private final long id;

    public BlockingQueue(long id) {
      this.id = id;
    }

    @Override
    public long id() {
      return id;
    }

    public void put(RowVector rowVector) {
      StaticJniApi.get().blockingQueuePut(this, rowVector);
    }

    public void noMoreInput() {
      StaticJniApi.get().blockingQueueNoMoreInput(this);
    }
  }
}

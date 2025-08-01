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
package io.github.zhztheplayer.velox4j.jni;

import java.util.Collections;
import java.util.List;

import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.FieldVector;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import io.github.zhztheplayer.velox4j.arrow.Arrow;
import io.github.zhztheplayer.velox4j.connector.ExternalStream;
import io.github.zhztheplayer.velox4j.data.BaseVector;
import io.github.zhztheplayer.velox4j.data.BaseVectorTests;
import io.github.zhztheplayer.velox4j.data.RowVector;
import io.github.zhztheplayer.velox4j.exception.VeloxException;
import io.github.zhztheplayer.velox4j.iterator.DownIterator;
import io.github.zhztheplayer.velox4j.iterator.DownIterators;
import io.github.zhztheplayer.velox4j.iterator.UpIterator;
import io.github.zhztheplayer.velox4j.iterator.UpIterators;
import io.github.zhztheplayer.velox4j.memory.BytesAllocationListener;
import io.github.zhztheplayer.velox4j.memory.MemoryManager;
import io.github.zhztheplayer.velox4j.query.QueryExecutor;
import io.github.zhztheplayer.velox4j.session.Session;
import io.github.zhztheplayer.velox4j.test.SampleQueryTests;
import io.github.zhztheplayer.velox4j.test.TestThreads;
import io.github.zhztheplayer.velox4j.test.UpIteratorTests;
import io.github.zhztheplayer.velox4j.test.Velox4jTests;
import io.github.zhztheplayer.velox4j.type.DoubleType;
import io.github.zhztheplayer.velox4j.type.IntegerType;
import io.github.zhztheplayer.velox4j.type.RealType;
import io.github.zhztheplayer.velox4j.variant.DoubleValue;
import io.github.zhztheplayer.velox4j.variant.IntegerValue;
import io.github.zhztheplayer.velox4j.variant.RealValue;

public class JniApiTest {
  private static BytesAllocationListener allocationListener;
  private static MemoryManager memoryManager;

  @BeforeClass
  public static void beforeClass() throws Exception {
    Velox4jTests.ensureInitialized();
    allocationListener = new BytesAllocationListener();
    memoryManager = MemoryManager.create(allocationListener);
  }

  @AfterClass
  public static void afterClass() throws Exception {
    memoryManager.close();
    Assert.assertEquals(0, allocationListener.currentBytes());
  }

  @Test
  public void testCreateAndClose() {
    final Session session = createLocalSession(memoryManager);
    session.close();
  }

  @Test
  public void testCreateTwice() {
    final Session session1 = createLocalSession(memoryManager);
    final Session session2 = createLocalSession(memoryManager);
    session1.close();
    session2.close();
  }

  @Test
  public void testCloseTwice() {
    final LocalSession session = createLocalSession(memoryManager);
    session.close();
    Assert.assertThrows(
        VeloxException.class,
        new ThrowingRunnable() {
          @Override
          public void run() {
            session.close();
          }
        });
  }

  @Test
  public void testExecuteQueryTryRun() {
    final String json = SampleQueryTests.readQueryJson();
    final LocalSession session = createLocalSession(memoryManager);
    final JniApi jniApi = getJniApi(session);
    final QueryExecutor queryExecutor = jniApi.createQueryExecutor(json);
    final UpIterator itr = queryExecutor.execute();
    itr.close();
    session.close();
  }

  @Test
  public void testExecuteQuery() {
    final LocalSession session = createLocalSession(memoryManager);
    final JniApi jniApi = getJniApi(session);
    final String json = SampleQueryTests.readQueryJson();
    final QueryExecutor queryExecutor = jniApi.createQueryExecutor(json);
    final UpIterator itr = queryExecutor.execute();
    SampleQueryTests.assertIterator(itr);
    session.close();
    ;
  }

  @Test
  public void testExecuteQueryTwice() {
    final LocalSession session = createLocalSession(memoryManager);
    final JniApi jniApi = getJniApi(session);
    final String json = SampleQueryTests.readQueryJson();
    final QueryExecutor queryExecutor = jniApi.createQueryExecutor(json);
    final UpIterator itr1 = queryExecutor.execute();
    final UpIterator itr2 = queryExecutor.execute();
    SampleQueryTests.assertIterator(itr1);
    SampleQueryTests.assertIterator(itr2);
    session.close();
    ;
  }

  @Test
  public void testVectorSerdeEmpty() {
    final LocalSession session = createLocalSession(memoryManager);
    final JniApi jniApi = getJniApi(session);
    final String serialized = StaticJniApi.get().baseVectorSerialize(Collections.emptyList());
    final List<BaseVector> deserialized = jniApi.baseVectorDeserialize(serialized);
    Assert.assertTrue(deserialized.isEmpty());
    final String serializedSecond = StaticJniApi.get().baseVectorSerialize(deserialized);
    Assert.assertEquals(serialized, serializedSecond);
    session.close();
    ;
  }

  @Test
  public void testVectorSerdeSingle() {
    final LocalSession session = createLocalSession(memoryManager);
    final JniApi jniApi = getJniApi(session);
    final String json = SampleQueryTests.readQueryJson();
    final QueryExecutor queryExecutor = jniApi.createQueryExecutor(json);
    final UpIterator itr = queryExecutor.execute();
    final RowVector vector = UpIteratorTests.collectSingleVector(itr);
    final List<RowVector> vectors = List.of(vector);
    final String serialized = StaticJniApi.get().baseVectorSerialize(vectors);
    final List<BaseVector> deserialized = jniApi.baseVectorDeserialize(serialized);
    BaseVectorTests.assertEquals(vectors, deserialized);
    session.close();
    ;
  }

  @Test
  public void testVectorSerdeMultiple() {
    final LocalSession session = createLocalSession(memoryManager);
    final JniApi jniApi = getJniApi(session);
    final String json = SampleQueryTests.readQueryJson();
    final QueryExecutor queryExecutor = jniApi.createQueryExecutor(json);
    final UpIterator itr = queryExecutor.execute();
    final RowVector vector = UpIteratorTests.collectSingleVector(itr);
    final List<RowVector> vectors = List.of(vector, vector);
    final String serialized = StaticJniApi.get().baseVectorSerialize(vectors);
    final List<BaseVector> deserialized = jniApi.baseVectorDeserialize(serialized);
    BaseVectorTests.assertEquals(vectors, deserialized);
    session.close();
  }

  @Test
  public void testArrowRoundTrip() {
    final LocalSession session = createLocalSession(memoryManager);
    final JniApi jniApi = getJniApi(session);
    final String json = SampleQueryTests.readQueryJson();
    final QueryExecutor queryExecutor = jniApi.createQueryExecutor(json);
    final UpIterator itr = queryExecutor.execute();
    final RowVector vector = UpIteratorTests.collectSingleVector(itr);
    final BufferAllocator alloc = new RootAllocator(Long.MAX_VALUE);
    final FieldVector arrowVector = Arrow.toArrowVector(alloc, vector);
    final BaseVector imported = session.arrowOps().fromArrowVector(alloc, arrowVector);
    BaseVectorTests.assertEquals(vector, imported);
    arrowVector.close();
    session.close();
  }

  @Test
  public void testVariantInferType() {
    Assert.assertTrue(
        StaticJniApi.get().variantInferType(new IntegerValue(5)) instanceof IntegerType);
    Assert.assertTrue(StaticJniApi.get().variantInferType(new RealValue(4.6f)) instanceof RealType);
    Assert.assertTrue(
        StaticJniApi.get().variantInferType(new DoubleValue(4.6d)) instanceof DoubleType);
  }

  @Test
  public void testIteratorRoundTrip() {
    final LocalSession session = createLocalSession(memoryManager);
    final JniApi jniApi = getJniApi(session);
    final String json = SampleQueryTests.readQueryJson();
    final QueryExecutor queryExecutor = jniApi.createQueryExecutor(json);
    final UpIterator itr = queryExecutor.execute();
    final DownIterator down = DownIterators.fromJavaIterator(UpIterators.asJavaIterator(itr));
    final ExternalStream es = jniApi.createExternalStreamFromDownIterator(down);
    final UpIterator up = jniApi.createUpIteratorWithExternalStream(es);
    SampleQueryTests.assertIterator(up);
    session.close();
  }

  @Test
  public void testIteratorRoundTripInDifferentThread() throws InterruptedException {
    final LocalSession session = createLocalSession(memoryManager);
    final JniApi jniApi = getJniApi(session);
    final String json = SampleQueryTests.readQueryJson();
    final QueryExecutor queryExecutor = jniApi.createQueryExecutor(json);
    final UpIterator itr = queryExecutor.execute();
    final DownIterator down = DownIterators.fromJavaIterator(UpIterators.asJavaIterator(itr));
    final ExternalStream es = jniApi.createExternalStreamFromDownIterator(down);
    final UpIterator up = jniApi.createUpIteratorWithExternalStream(es);
    final Thread thread =
        TestThreads.newTestThread(
            new Runnable() {
              @Override
              public void run() {
                SampleQueryTests.assertIterator(up);
              }
            });
    thread.start();
    thread.join();
    session.close();
  }

  private static LocalSession createLocalSession(MemoryManager memoryManager) {
    return JniApiTests.createLocalSession(memoryManager);
  }

  private static JniApi getJniApi(LocalSession session) {
    return JniApiTests.getJniApi(session);
  }
}

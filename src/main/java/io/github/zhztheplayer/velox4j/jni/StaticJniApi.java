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

import java.util.List;

import org.apache.arrow.c.ArrowArray;
import org.apache.arrow.c.ArrowSchema;

import io.github.zhztheplayer.velox4j.config.Config;
import io.github.zhztheplayer.velox4j.connector.ConnectorSplit;
import io.github.zhztheplayer.velox4j.connector.ExternalStreams;
import io.github.zhztheplayer.velox4j.data.BaseVector;
import io.github.zhztheplayer.velox4j.data.RowVector;
import io.github.zhztheplayer.velox4j.data.SelectivityVector;
import io.github.zhztheplayer.velox4j.data.VectorEncoding;
import io.github.zhztheplayer.velox4j.iterator.UpIterator;
import io.github.zhztheplayer.velox4j.memory.AllocationListener;
import io.github.zhztheplayer.velox4j.memory.MemoryManager;
import io.github.zhztheplayer.velox4j.query.SerialTask;
import io.github.zhztheplayer.velox4j.query.SerialTaskStats;
import io.github.zhztheplayer.velox4j.serde.Serde;
import io.github.zhztheplayer.velox4j.serializable.ISerializable;
import io.github.zhztheplayer.velox4j.serializable.ISerializableCo;
import io.github.zhztheplayer.velox4j.type.RowType;
import io.github.zhztheplayer.velox4j.type.Type;
import io.github.zhztheplayer.velox4j.variant.Variant;
import io.github.zhztheplayer.velox4j.variant.VariantCo;

/** The higher-level JNI-based API over {@link StaticJniWrapper}. */
public class StaticJniApi {
  private static final StaticJniApi INSTANCE = new StaticJniApi();

  public static StaticJniApi get() {
    return INSTANCE;
  }

  private final StaticJniWrapper jni = StaticJniWrapper.get();

  private StaticJniApi() {}

  public void initialize(Config globalConf) {
    jni.initialize(Serde.toPrettyJson(globalConf));
  }

  public MemoryManager createMemoryManager(AllocationListener listener) {
    return new MemoryManager(jni.createMemoryManager(listener));
  }

  public LocalSession createSession(MemoryManager memoryManager) {
    return new LocalSession(jni.createSession(memoryManager.id()));
  }

  public void releaseCppObject(CppObject obj) {
    jni.releaseCppObject(obj.id());
  }

  public UpIterator.State upIteratorAdvance(UpIterator itr) {
    return UpIterator.State.get(jni.upIteratorAdvance(itr.id()));
  }

  public void upIteratorWait(UpIterator itr) {
    jni.upIteratorWait(itr.id());
  }

  public void blockingQueuePut(ExternalStreams.BlockingQueue queue, RowVector rowVector) {
    jni.blockingQueuePut(queue.id(), rowVector.id());
  }

  public void blockingQueueNoMoreInput(ExternalStreams.BlockingQueue queue) {
    jni.blockingQueueNoMoreInput(queue.id());
  }

  public void serialTaskAddSplit(
      SerialTask serialTask, String planNodeId, int groupId, ConnectorSplit split) {
    final String splitJson = Serde.toJson(split);
    jni.serialTaskAddSplit(serialTask.id(), planNodeId, groupId, splitJson);
  }

  public void serialTaskNoMoreSplits(SerialTask serialTask, String planNodeId) {
    jni.serialTaskNoMoreSplits(serialTask.id(), planNodeId);
  }

  public SerialTaskStats serialTaskCollectStats(SerialTask serialTask) {
    final String statsJson = jni.serialTaskCollectStats(serialTask.id());
    return SerialTaskStats.fromJson(statsJson);
  }

  public Type variantInferType(Variant variant) {
    final String variantJson = Serde.toJson(variant);
    final String typeJson = jni.variantInferType(variantJson);
    return Serde.fromJson(typeJson, Type.class);
  }

  public void baseVectorToArrow(BaseVector vector, ArrowSchema schema, ArrowArray array) {
    jni.baseVectorToArrow(vector.id(), schema.memoryAddress(), array.memoryAddress());
  }

  public String baseVectorSerialize(List<? extends BaseVector> vector) {
    return jni.baseVectorSerialize(vector.stream().mapToLong(BaseVector::id).toArray());
  }

  public Type baseVectorGetType(BaseVector vector) {
    final String typeJson = jni.baseVectorGetType(vector.id());
    return Serde.fromJson(typeJson, Type.class);
  }

  public int baseVectorGetSize(BaseVector vector) {
    return jni.baseVectorGetSize(vector.id());
  }

  public VectorEncoding baseVectorGetEncoding(BaseVector vector) {
    return VectorEncoding.valueOf(jni.baseVectorGetEncoding(vector.id()));
  }

  public void baseVectorAppend(BaseVector vector, BaseVector toAppend) {
    jni.baseVectorAppend(vector.id(), toAppend.id());
  }

  public boolean selectivityVectorIsValid(SelectivityVector vector, int idx) {
    return jni.selectivityVectorIsValid(vector.id(), idx);
  }

  public RowType tableWriteTraitsOutputType() {
    final String typeJson = jni.tableWriteTraitsOutputType();
    final RowType type = Serde.fromJson(typeJson, RowType.class);
    return type;
  }

  public ISerializable iSerializableAsJava(ISerializableCo co) {
    final String json = jni.iSerializableAsJava(co.id());
    return Serde.fromJson(json, ISerializable.class);
  }

  public Variant variantAsJava(VariantCo co) {
    final String json = jni.variantAsJava(co.id());
    return Serde.fromJson(json, Variant.class);
  }
}

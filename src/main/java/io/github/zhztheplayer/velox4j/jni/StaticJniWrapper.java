package io.github.zhztheplayer.velox4j.jni;

import io.github.zhztheplayer.velox4j.memory.AllocationListener;

public class StaticJniWrapper {
  private static final StaticJniWrapper INSTANCE = new StaticJniWrapper();

  static StaticJniWrapper get() {
    return INSTANCE;
  }

  private StaticJniWrapper() {
  }

  // Global initialization.
  native void initialize(String globalConfJson);

  // Memory.
  native long createMemoryManager(AllocationListener listener);

  // Lifecycle.
  native long createSession(long memoryManagerId);
  native void releaseCppObject(long objectId);

  // For UpIterator.
  native int upIteratorAdvance(long id);
  native void upIteratorWait(long id);

  // For Variant.
  native String variantInferType(String json);

  // For BaseVector / RowVector / SelectivityVector.
  native void baseVectorToArrow(long rvid, long cSchema, long cArray);
  native String baseVectorSerialize(long[] id);
  native String baseVectorGetType(long id);
  native int baseVectorGetSize(long id);
  native String baseVectorGetEncoding(long id);
  native void baseVectorAppend(long id, long toAppendId);
  native boolean selectivityVectorIsValid(long id, int idx);

  // For TableWrite.
  native String tableWriteTraitsOutputType();

  // For serde.
  native String iSerializableAsJava(long id);
  native String variantAsJava(long id);
}

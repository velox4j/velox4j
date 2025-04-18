package io.github.zhztheplayer.velox4j.data;

import io.github.zhztheplayer.velox4j.arrow.Arrow;
import io.github.zhztheplayer.velox4j.jni.JniApi;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.table.Table;

public class RowVector extends BaseVector {
  protected RowVector(JniApi jniApi, long id) {
    super(jniApi, id, VectorEncoding.ROW);
  }

  @Override
  public String toString(BufferAllocator alloc) {
    try (final Table t = Arrow.toArrowTable(alloc, this);
        final VectorSchemaRoot vsr = t.toVectorSchemaRoot()) {
      return vsr.contentToTSVString();
    }
  }
}

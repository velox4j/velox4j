package io.github.zhztheplayer.velox4j.jni;

import com.google.common.annotations.VisibleForTesting;
import io.github.zhztheplayer.velox4j.config.Config;
import io.github.zhztheplayer.velox4j.data.BaseVector;
import io.github.zhztheplayer.velox4j.data.SelectivityVector;
import io.github.zhztheplayer.velox4j.data.VectorEncoding;
import io.github.zhztheplayer.velox4j.iterator.UpIterator;
import io.github.zhztheplayer.velox4j.memory.AllocationListener;
import io.github.zhztheplayer.velox4j.memory.MemoryManager;
import io.github.zhztheplayer.velox4j.plan.AggregationNode;
import io.github.zhztheplayer.velox4j.serde.Serde;
import io.github.zhztheplayer.velox4j.serializable.ISerializable;
import io.github.zhztheplayer.velox4j.serializable.ISerializableCo;
import io.github.zhztheplayer.velox4j.type.RowType;
import io.github.zhztheplayer.velox4j.type.Type;
import io.github.zhztheplayer.velox4j.variant.Variant;
import io.github.zhztheplayer.velox4j.variant.VariantCo;
import org.apache.arrow.c.ArrowArray;
import org.apache.arrow.c.ArrowSchema;

import java.util.List;

public class StaticJniApi {
  private static final StaticJniApi INSTANCE = new StaticJniApi();

  public static StaticJniApi get() {
    return INSTANCE;
  }

  private final StaticJniWrapper jni = StaticJniWrapper.get();

  private StaticJniApi() {
  }

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

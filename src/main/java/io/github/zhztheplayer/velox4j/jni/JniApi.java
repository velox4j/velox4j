package io.github.zhztheplayer.velox4j.jni;

import com.google.common.annotations.VisibleForTesting;
import io.github.zhztheplayer.velox4j.connector.ExternalStream;
import io.github.zhztheplayer.velox4j.data.BaseVector;
import io.github.zhztheplayer.velox4j.data.RowVector;
import io.github.zhztheplayer.velox4j.data.SelectivityVector;
import io.github.zhztheplayer.velox4j.data.VectorEncoding;
import io.github.zhztheplayer.velox4j.eval.Evaluation;
import io.github.zhztheplayer.velox4j.eval.Evaluator;
import io.github.zhztheplayer.velox4j.iterator.DownIterator;
import io.github.zhztheplayer.velox4j.iterator.UpIterator;
import io.github.zhztheplayer.velox4j.plan.AggregationNode;
import io.github.zhztheplayer.velox4j.query.Query;
import io.github.zhztheplayer.velox4j.serde.Serde;
import io.github.zhztheplayer.velox4j.serializable.ISerializable;
import io.github.zhztheplayer.velox4j.serializable.ISerializableCo;
import io.github.zhztheplayer.velox4j.type.RowType;
import io.github.zhztheplayer.velox4j.type.Type;
import io.github.zhztheplayer.velox4j.variant.Variant;
import io.github.zhztheplayer.velox4j.variant.VariantCo;
import org.apache.arrow.c.ArrowArray;
import org.apache.arrow.c.ArrowSchema;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The higher-level JNI-based API over {@link JniWrapper}. The API hides
 * details like native pointers and serialized data from developers, instead
 * provides objective forms of the required functionalities.
 */
public final class JniApi {
  private final JniWrapper jni;

  JniApi(JniWrapper jni) {
    this.jni = jni;
  }

  public Evaluator createEvaluator(Evaluation evaluation) {
    final String evalJson = Serde.toPrettyJson(evaluation);
    return new Evaluator(this, jni.createEvaluator(evalJson));
  }

  public BaseVector evaluatorEval(Evaluator evaluator, SelectivityVector sv, RowVector input) {
    return baseVectorWrap(jni.evaluatorEval(evaluator.id(), sv.id(), input.id()));
  }

  public UpIterator executeQuery(Query query) {
    final String queryJson = Serde.toPrettyJson(query);
    return new UpIterator(this, jni.executeQuery(queryJson));
  }

  @VisibleForTesting
  UpIterator executeQuery(String queryJson) {
    return new UpIterator(this, jni.executeQuery(queryJson));
  }

  public RowVector upIteratorGet(UpIterator itr) {
    return baseVectorWrap(jni.upIteratorGet(itr.id())).asRowVector();
  }

  public ExternalStream newExternalStream(DownIterator itr) {
    return new ExternalStream(jni.newExternalStream(itr));
  }

  public BaseVector createEmptyBaseVector(Type type) {
    final String typeJson = Serde.toJson(type);
    return baseVectorWrap(jni.createEmptyBaseVector(typeJson));
  }

  public BaseVector arrowToBaseVector(ArrowSchema schema, ArrowArray array) {
    return baseVectorWrap(jni.arrowToBaseVector(schema.memoryAddress(), array.memoryAddress()));
  }

  public List<BaseVector> baseVectorDeserialize(String serialized) {
    return Arrays.stream(jni.baseVectorDeserialize(serialized))
        .mapToObj(this::baseVectorWrap)
        .collect(Collectors.toList());
  }

  public BaseVector baseVectorWrapInConstant(BaseVector vector, int length, int index) {
    return baseVectorWrap(jni.baseVectorWrapInConstant(vector.id(), length, index));
  }

  public BaseVector baseVectorSlice(BaseVector vector, int offset, int length) {
    return baseVectorWrap(jni.baseVectorSlice(vector.id(), offset, length));
  }

  public BaseVector loadedVector(BaseVector vector) {
    return baseVectorWrap(jni.baseVectorLoadedVector(vector.id()));
  }

  public SelectivityVector createSelectivityVector(int length) {
    return new SelectivityVector(jni.createSelectivityVector(length));
  }

  public RowType tableWriteTraitsOutputTypeWithAggregationNode(AggregationNode aggregationNode) {
    final String aggregationNodeJson = Serde.toJson(aggregationNode);
    final String typeJson = jni.tableWriteTraitsOutputTypeWithAggregationNode(aggregationNodeJson);
    final RowType type = Serde.fromJson(typeJson, RowType.class);
    return type;
  }

  public ISerializableCo iSerializableAsCpp(ISerializable iSerializable) {
    final String json = Serde.toPrettyJson(iSerializable);
    return new ISerializableCo(jni.iSerializableAsCpp(json));
  }

  public VariantCo variantAsCpp(Variant variant) {
    final String json = Serde.toPrettyJson(variant);
    return new VariantCo(jni.variantAsCpp(json));
  }

  @VisibleForTesting
  public UpIterator createUpIteratorWithExternalStream(ExternalStream es) {
    return new UpIterator(this, jni.createUpIteratorWithExternalStream(es.id()));
  }

  private BaseVector baseVectorWrap(long id) {
    final VectorEncoding encoding = VectorEncoding.valueOf(
        StaticJniWrapper.get().baseVectorGetEncoding(id));
    return BaseVector.wrap(this, id, encoding);
  }
}

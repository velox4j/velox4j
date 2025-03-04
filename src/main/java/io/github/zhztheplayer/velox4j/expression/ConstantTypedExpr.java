package io.github.zhztheplayer.velox4j.expression;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.zhztheplayer.velox4j.data.BaseVector;
import io.github.zhztheplayer.velox4j.data.BaseVectors;
import io.github.zhztheplayer.velox4j.data.VectorEncoding;
import io.github.zhztheplayer.velox4j.jni.StaticJniApi;
import io.github.zhztheplayer.velox4j.type.Type;
import io.github.zhztheplayer.velox4j.variant.Variant;

import java.util.Collections;

public class ConstantTypedExpr extends TypedExpr {
  private final Variant value;
  private final String serializedVector;

  @JsonCreator
  public ConstantTypedExpr(@JsonProperty("type") Type returnType,
      @JsonProperty("value") Variant value,
      @JsonProperty("valueVector") String serializedVector) {
    super(returnType, Collections.emptyList());
    this.value = value;
    this.serializedVector = serializedVector;
  }

  public static ConstantTypedExpr create(BaseVector vector) {
    final BaseVector constVector;
    if (vector.getEncoding() == VectorEncoding.CONSTANT) {
      constVector = vector;
    } else {
      constVector = vector.wrapInConstant(1, 0);
    }
    final String serialized = BaseVectors.serializeOne(constVector);
    final Type type = vector.getType();
    return new ConstantTypedExpr(type, null, serialized);
  }

  public static ConstantTypedExpr create(Variant value) {
    return new ConstantTypedExpr(StaticJniApi.get().variantInferType(value), value, null);
  }

  @JsonGetter("value")
  public Variant getValue() {
    return value;
  }

  @JsonGetter("valueVector")
  public String getSerializedVector() {
    return serializedVector;
  }
}

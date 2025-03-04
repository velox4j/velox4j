package io.github.zhztheplayer.velox4j.variant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Base64;
import java.util.Objects;

public class VarBinaryValue extends Variant {
  private final String value;

  @JsonCreator
  public VarBinaryValue(@JsonProperty("value") String value) {
    this.value = value;
  }

  public static VarBinaryValue create(byte[] value) {
    return new VarBinaryValue(Base64.getEncoder().encodeToString(value));
  }

  @JsonGetter("value")
  public String getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    VarBinaryValue that = (VarBinaryValue) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(value);
  }

  @Override
  public String toString() {
    return "VarBinaryValue{" +
        "value='" + value + '\'' +
        '}';
  }
}

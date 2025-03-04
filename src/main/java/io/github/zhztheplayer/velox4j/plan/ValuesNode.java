package io.github.zhztheplayer.velox4j.plan;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import io.github.zhztheplayer.velox4j.data.BaseVectors;
import io.github.zhztheplayer.velox4j.data.RowVector;

import java.util.List;

public class ValuesNode extends PlanNode {
  private final String serializedRowVectors;
  private final boolean parallelizable;
  private final int repeatTimes;

  @JsonCreator
  public ValuesNode(@JsonProperty("id") String id,
      @JsonProperty("data") String serializedRowVectors,
      @JsonProperty("parallelizable") boolean parallelizable,
      @JsonProperty("repeatTimes") int repeatTimes) {
    super(id);
    this.serializedRowVectors = Preconditions.checkNotNull(serializedRowVectors);
    this.parallelizable = parallelizable;
    this.repeatTimes = repeatTimes;
  }

  public static ValuesNode create(String id, List<RowVector> vectors, boolean parallelizable,
      int repeatTimes) {
    return new ValuesNode(id, BaseVectors.serializeAll(vectors), parallelizable, repeatTimes);
  }

  @JsonGetter("data")
  public String getSerializedRowVectors() {
    return serializedRowVectors;
  }

  @JsonGetter("parallelizable")
  public boolean isParallelizable() {
    return parallelizable;
  }

  @JsonGetter("repeatTimes")
  public int getRepeatTimes() {
    return repeatTimes;
  }

  @Override
  protected List<PlanNode> getSources() {
    return List.of();
  }
}

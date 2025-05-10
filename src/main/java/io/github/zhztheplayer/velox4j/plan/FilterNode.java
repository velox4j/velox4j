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
package io.github.zhztheplayer.velox4j.plan;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import io.github.zhztheplayer.velox4j.expression.TypedExpr;

public class FilterNode extends PlanNode {
  private List<PlanNode> sources;
  private final TypedExpr filter;

  @JsonCreator
  public FilterNode(
      @JsonProperty("id") String id,
      @JsonProperty("sources") List<PlanNode> sources,
      @JsonProperty("filter") TypedExpr filter) {
    super(id);
    this.sources = sources;
    this.filter = filter;
  }

  @Override
  protected List<PlanNode> getSources() {
    return sources;
  }

  @JsonGetter("filter")
  public TypedExpr getFilter() {
    return filter;
  }

  @Override
  public void setSources(List<PlanNode> sources) {
    if (this.sources != null && !this.sources.isEmpty()) {
      this.sources.forEach(planNode -> planNode.setSources(sources));
    } else {
      Preconditions.checkArgument(sources.size() == 1, "Filter only accept one source");
      this.sources = sources;
    }
  }
}

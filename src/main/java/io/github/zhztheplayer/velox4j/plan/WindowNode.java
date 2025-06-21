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

import io.github.zhztheplayer.velox4j.expression.FieldAccessTypedExpr;
import io.github.zhztheplayer.velox4j.sort.SortOrder;
import io.github.zhztheplayer.velox4j.window.WindowFunction;

public class WindowNode extends PlanNode {
  private final List<FieldAccessTypedExpr> partitionKeys;
  private final List<FieldAccessTypedExpr> sortingKeys;
  private final List<SortOrder> sortingOrders;
  private final List<String> windowColumnNames;
  private final List<WindowFunction> windowFunctions;
  private final boolean inputsSorted;

  private final List<PlanNode> sources;

  @JsonCreator
  public WindowNode(
      @JsonProperty("id") String id,
      @JsonProperty("partitionKeys") List<FieldAccessTypedExpr> partitionKeys,
      @JsonProperty("sortingKeys") List<FieldAccessTypedExpr> sortingKeys,
      @JsonProperty("sortingOrders") List<SortOrder> sortingOrders,
      @JsonProperty("names") List<String> windowColumnNames,
      @JsonProperty("functions") List<WindowFunction> windowFunctions,
      @JsonProperty("inputsSorted") boolean inputsSorted,
      @JsonProperty("sources") List<PlanNode> sources) {
    super(id);
    this.partitionKeys = partitionKeys;
    this.sortingKeys = sortingKeys;
    this.sortingOrders = sortingOrders;
    this.windowColumnNames = windowColumnNames;
    this.windowFunctions = windowFunctions;
    this.inputsSorted = inputsSorted;
    this.sources = sources;
  }

  @Override
  public List<PlanNode> getSources() {
    return sources;
  }

  @JsonGetter("partitionKeys")
  public List<FieldAccessTypedExpr> getPartitionKeys() {
    return partitionKeys;
  }

  @JsonGetter("sortingKeys")
  public List<FieldAccessTypedExpr> getSortingKeys() {
    return sortingKeys;
  }

  @JsonGetter("names")
  public List<String> getWindowColumnNames() {
    return windowColumnNames;
  }

  @JsonGetter("sortingOrders")
  public List<SortOrder> getSortingOrders() {
    return sortingOrders;
  }

  @JsonGetter("inputsSorted")
  public boolean isInputsSorted() {
    return inputsSorted;
  }

  @JsonGetter("functions")
  public List<WindowFunction> getWindowFunctions() {
    return windowFunctions;
  }
}

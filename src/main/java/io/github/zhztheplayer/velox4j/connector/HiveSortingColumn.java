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
package io.github.zhztheplayer.velox4j.connector;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.zhztheplayer.velox4j.serializable.ISerializable;
import io.github.zhztheplayer.velox4j.sort.SortOrder;

public class HiveSortingColumn extends ISerializable {
  private final String sortColumn;
  private final SortOrder sortOrder;

  @JsonCreator
  public HiveSortingColumn(
      @JsonProperty("columnName") String sortColumn,
      @JsonProperty("sortOrder") SortOrder sortOrder) {
    this.sortColumn = sortColumn;
    this.sortOrder = sortOrder;
  }

  @JsonGetter("columnName")
  public String getSortColumn() {
    return sortColumn;
  }

  @JsonGetter("sortOrder")
  public SortOrder getSortOrder() {
    return sortOrder;
  }
}

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
package io.github.zhztheplayer.velox4j.type;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FunctionType extends Type {
  private final List<Type> children;

  @JsonCreator
  private FunctionType(@JsonProperty("cTypes") List<Type> children) {
    this.children = children;
  }

  public static FunctionType create(List<Type> argumentTypes, Type returnType) {
    final List<Type> mergedTypes = new ArrayList<>(argumentTypes);
    mergedTypes.add(returnType);
    return new FunctionType(mergedTypes);
  }

  @JsonGetter("cTypes")
  public List<Type> getChildren() {
    return children;
  }
}

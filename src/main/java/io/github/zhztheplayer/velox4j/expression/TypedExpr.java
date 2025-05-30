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
package io.github.zhztheplayer.velox4j.expression;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;

import io.github.zhztheplayer.velox4j.serializable.ISerializable;
import io.github.zhztheplayer.velox4j.type.Type;

public abstract class TypedExpr extends ISerializable {
  private final Type returnType;
  private final List<TypedExpr> inputs;

  protected TypedExpr(Type returnType, List<TypedExpr> inputs) {
    this.returnType = returnType;
    this.inputs = inputs == null ? Collections.emptyList() : Collections.unmodifiableList(inputs);
  }

  @JsonGetter("type")
  public Type getReturnType() {
    return returnType;
  }

  @JsonGetter("inputs")
  public List<TypedExpr> getInputs() {
    return inputs;
  }
}

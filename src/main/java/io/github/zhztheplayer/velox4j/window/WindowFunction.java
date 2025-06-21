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
package io.github.zhztheplayer.velox4j.window;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.zhztheplayer.velox4j.expression.CallTypedExpr;
import io.github.zhztheplayer.velox4j.serializable.ISerializable;

public class WindowFunction extends ISerializable {
  private final CallTypedExpr functionCall;
  private final WindowFrame frame;
  private final boolean ignoreNulls;

  @JsonCreator
  public WindowFunction(
      @JsonProperty("functionCall") CallTypedExpr functionCall,
      @JsonProperty("frame") WindowFrame frame,
      @JsonProperty("ignoreNulls") boolean ignoreNulls) {
    this.functionCall = functionCall;
    this.frame = frame;
    this.ignoreNulls = ignoreNulls;
  }

  @JsonGetter("functionCall")
  public CallTypedExpr getFunctionCall() {
    return functionCall;
  }

  @JsonGetter("frame")
  public WindowFrame getFrame() {
    return frame;
  }

  @JsonGetter("ignoreNulls")
  public boolean isIgnoreNulls() {
    return ignoreNulls;
  }
}

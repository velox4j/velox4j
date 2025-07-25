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
package io.github.zhztheplayer.velox4j.config;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.zhztheplayer.velox4j.serializable.ISerializable;

public class Config extends ISerializable {
  private static final Config EMPTY = new Config(List.of());
  public static final String VELOX4J_INIT_PRESET = "velox4j.init.preset";
  public static final String VELOX4J_INIT_PRESET_SPARK = "0";
  public static final String VELOX4J_INIT_PRESET_FLINK = "1";

  public static Config empty() {
    return EMPTY;
  }

  private final List<Entry> values;

  @JsonCreator
  private Config(@JsonProperty("values") List<Entry> values) {
    this.values = values;
  }

  public static Config create(Map<String, String> values) {
    return new Config(
        values.entrySet().stream()
            .map(e -> new Entry(e.getKey(), e.getValue()))
            .collect(Collectors.toList()));
  }

  @JsonGetter("values")
  public List<Entry> values() {
    return values;
  }

  public static class Entry {
    private final String key;
    private final String value;

    @JsonCreator
    public Entry(@JsonProperty("key") String key, @JsonProperty("value") String value) {
      this.key = key;
      this.value = value;
    }

    @JsonGetter("key")
    public String key() {
      return key;
    }

    @JsonGetter("value")
    public String value() {
      return value;
    }
  }
}

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

#include "velox4j/arrow/Arrow.h"
#include <velox/vector/arrow/Bridge.h>

namespace velox4j {
using namespace facebook::velox;

namespace {

void slice(VectorPtr& in) {
  auto* rowBase = in->as<RowVector>();
  if (!rowBase) {
    return;
  }
  for (auto& child : rowBase->children()) {
    if (child->size() > rowBase->size()) {
      // Some Velox operations (E.g., Limit) could result in a
      // RowVector whose children have larger size than itself.
      // So we perform a slice to keep only the data that is
      // in real use.
      child = child->slice(0, rowBase->size());
    }
  }
}

void flatten(VectorPtr& in) {
  facebook::velox::BaseVector::flattenVector(in);
}

ArrowOptions makeOptions() {
  ArrowOptions options;
  options.timestampUnit = static_cast<TimestampUnit>(6);
  return options;
}
} // namespace

void fromBaseVectorToArrow(
    VectorPtr vector,
    ArrowSchema* cSchema,
    ArrowArray* cArray) {
  flatten(vector);
  slice(vector);
  auto options = makeOptions();
  exportToArrow(vector, *cSchema, options);
  exportToArrow(vector, *cArray, vector->pool(), options);
}

VectorPtr fromArrowToBaseVector(
    memory::MemoryPool* pool,
    ArrowSchema* cSchema,
    ArrowArray* cArray) {
  auto options = makeOptions();
  return importFromArrowAsOwner(*cSchema, *cArray, pool);
}
} // namespace velox4j

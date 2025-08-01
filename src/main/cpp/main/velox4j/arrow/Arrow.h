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

#pragma once

#include <velox/vector/ComplexVector.h>

struct ArrowArray;
struct ArrowSchema;

namespace velox4j {
// Exports the input base vector to Arrow ABI structs.
void fromBaseVectorToArrow(
    facebook::velox::VectorPtr vector,
    ArrowSchema* cSchema,
    ArrowArray* cArray);

// Imports the given Arrow ABI structs into a base vector, then returns it.
facebook::velox::VectorPtr fromArrowToBaseVector(
    facebook::velox::memory::MemoryPool* pool,
    ArrowSchema* cSchema,
    ArrowArray* cArray);
} // namespace velox4j

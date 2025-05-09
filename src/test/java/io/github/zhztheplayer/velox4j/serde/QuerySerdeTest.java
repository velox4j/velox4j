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
package io.github.zhztheplayer.velox4j.serde;

import org.junit.BeforeClass;
import org.junit.Ignore;

import io.github.zhztheplayer.velox4j.query.Query;
import io.github.zhztheplayer.velox4j.test.ResourceTests;
import io.github.zhztheplayer.velox4j.test.Velox4jTests;

public class QuerySerdeTest {

  @BeforeClass
  public static void beforeClass() throws Exception {
    Velox4jTests.ensureInitialized();
  }

  // Ignored by https://github.com/velox4j/velox4j/issues/104.
  @Ignore
  public void testReadPlanJsonFromFile() {
    final String queryJson = ResourceTests.readResourceAsString("query/example-1.json");
    SerdeTests.testISerializableRoundTrip(queryJson, Query.class);
  }
}

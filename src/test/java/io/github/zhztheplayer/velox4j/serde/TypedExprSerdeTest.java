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

import java.util.Collections;
import java.util.List;

import org.junit.*;

import io.github.zhztheplayer.velox4j.Velox4j;
import io.github.zhztheplayer.velox4j.data.BaseVector;
import io.github.zhztheplayer.velox4j.data.BaseVectorTests;
import io.github.zhztheplayer.velox4j.data.BaseVectors;
import io.github.zhztheplayer.velox4j.expression.CallTypedExpr;
import io.github.zhztheplayer.velox4j.expression.CastTypedExpr;
import io.github.zhztheplayer.velox4j.expression.ConcatTypedExpr;
import io.github.zhztheplayer.velox4j.expression.ConstantTypedExpr;
import io.github.zhztheplayer.velox4j.expression.DereferenceTypedExpr;
import io.github.zhztheplayer.velox4j.expression.FieldAccessTypedExpr;
import io.github.zhztheplayer.velox4j.expression.InputTypedExpr;
import io.github.zhztheplayer.velox4j.expression.LambdaTypedExpr;
import io.github.zhztheplayer.velox4j.memory.BytesAllocationListener;
import io.github.zhztheplayer.velox4j.memory.MemoryManager;
import io.github.zhztheplayer.velox4j.session.Session;
import io.github.zhztheplayer.velox4j.test.Velox4jTests;
import io.github.zhztheplayer.velox4j.type.BooleanType;
import io.github.zhztheplayer.velox4j.type.IntegerType;
import io.github.zhztheplayer.velox4j.type.RealType;
import io.github.zhztheplayer.velox4j.type.RowType;
import io.github.zhztheplayer.velox4j.type.VarCharType;
import io.github.zhztheplayer.velox4j.variant.IntegerValue;

public class TypedExprSerdeTest {
  private static BytesAllocationListener allocationListener;
  private static MemoryManager memoryManager;
  private static Session session;

  @BeforeClass
  public static void beforeClass() throws Exception {
    Velox4jTests.ensureInitialized();
    allocationListener = new BytesAllocationListener();
    memoryManager = MemoryManager.create(allocationListener);
  }

  @AfterClass
  public static void afterClass() throws Exception {
    memoryManager.close();
    Assert.assertEquals(0, allocationListener.currentBytes());
  }

  @Before
  public void setUp() throws Exception {
    session = Velox4j.newSession(memoryManager);
  }

  @After
  public void tearDown() throws Exception {
    session.close();
  }

  @Test
  public void testCallTypedExpr() {
    SerdeTests.testISerializableRoundTrip(
        new CallTypedExpr(new IntegerType(), Collections.emptyList(), "random_int"));
  }

  @Test
  public void testCastTypedExpr() {
    final CallTypedExpr input =
        new CallTypedExpr(new IntegerType(), Collections.emptyList(), "random_int");
    SerdeTests.testISerializableRoundTrip(CastTypedExpr.create(new IntegerType(), input, true));
  }

  @Test
  public void testConcatTypedExpr() {
    final CallTypedExpr input1 =
        new CallTypedExpr(new IntegerType(), Collections.emptyList(), "random_int");
    final CallTypedExpr input2 =
        new CallTypedExpr(new RealType(), Collections.emptyList(), "random_real");
    SerdeTests.testISerializableRoundTrip(
        ConcatTypedExpr.create(List.of("foo", "bar"), List.of(input1, input2)));
  }

  // Ignored by https://github.com/velox4j/velox4j/issues/104.
  @Ignore
  public void testConstantTypedExprWithVector() {
    final BaseVector intVector = BaseVectorTests.newSampleIntVector(session);
    final ConstantTypedExpr expr1 = ConstantTypedExpr.create(intVector);
    SerdeTests.testISerializableRoundTrip(expr1);
    final ConstantTypedExpr expr2 =
        new ConstantTypedExpr(
            new IntegerType(), null, BaseVectors.serializeOne(intVector.wrapInConstant(1, 0)));
    SerdeTests.testISerializableRoundTrip(expr2);
  }

  @Test
  public void testConstantTypedExprWithVariant() {
    final ConstantTypedExpr expr1 = ConstantTypedExpr.create(new IntegerValue(15));
    SerdeTests.testISerializableRoundTrip(expr1);
    final ConstantTypedExpr expr2 =
        new ConstantTypedExpr(new IntegerType(), new IntegerValue(15), null);
    SerdeTests.testISerializableRoundTrip(expr2);
  }

  @Test
  public void testDereferenceTypedExpr() {
    final CallTypedExpr input1 =
        new CallTypedExpr(new IntegerType(), Collections.emptyList(), "random_int");
    final CallTypedExpr input2 =
        new CallTypedExpr(new RealType(), Collections.emptyList(), "random_real");
    final ConcatTypedExpr concat =
        ConcatTypedExpr.create(List.of("foo", "bar"), List.of(input1, input2));
    final DereferenceTypedExpr dereference = DereferenceTypedExpr.create(concat, 1);
    Assert.assertEquals(RealType.class, dereference.getReturnType().getClass());
    SerdeTests.testISerializableRoundTrip(dereference);
  }

  @Test
  public void testFieldAccessTypedExpr() {
    final CallTypedExpr input1 =
        new CallTypedExpr(new IntegerType(), Collections.emptyList(), "random_int");
    final CallTypedExpr input2 =
        new CallTypedExpr(new RealType(), Collections.emptyList(), "random_real");
    final ConcatTypedExpr concat =
        ConcatTypedExpr.create(List.of("foo", "bar"), List.of(input1, input2));
    final FieldAccessTypedExpr fieldAccess = FieldAccessTypedExpr.create(concat, "bar");
    Assert.assertEquals(RealType.class, fieldAccess.getReturnType().getClass());
    SerdeTests.testISerializableRoundTrip(fieldAccess);
  }

  @Test
  public void testInputTypedExpr() {
    SerdeTests.testISerializableRoundTrip(new InputTypedExpr(new BooleanType()));
  }

  @Test
  public void testLambdaTypedExpr() {
    final RowType signature =
        new RowType(List.of("foo", "bar"), List.of(new IntegerType(), new VarCharType()));
    final LambdaTypedExpr lambdaTypedExpr =
        LambdaTypedExpr.create(signature, FieldAccessTypedExpr.create(new IntegerType(), "foo"));
    SerdeTests.testISerializableRoundTrip(lambdaTypedExpr);
  }
}

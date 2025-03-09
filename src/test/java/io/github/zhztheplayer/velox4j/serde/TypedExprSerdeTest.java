package io.github.zhztheplayer.velox4j.serde;

import io.github.zhztheplayer.velox4j.Velox4j;
import io.github.zhztheplayer.velox4j.data.BaseVector;
import io.github.zhztheplayer.velox4j.data.BaseVectors;
import io.github.zhztheplayer.velox4j.data.BaseVectorTests;
import io.github.zhztheplayer.velox4j.expression.CallTypedExpr;
import io.github.zhztheplayer.velox4j.expression.CastTypedExpr;
import io.github.zhztheplayer.velox4j.expression.ConcatTypedExpr;
import io.github.zhztheplayer.velox4j.expression.ConstantTypedExpr;
import io.github.zhztheplayer.velox4j.expression.DereferenceTypedExpr;
import io.github.zhztheplayer.velox4j.expression.FieldAccessTypedExpr;
import io.github.zhztheplayer.velox4j.expression.InputTypedExpr;
import io.github.zhztheplayer.velox4j.expression.LambdaTypedExpr;
import io.github.zhztheplayer.velox4j.session.Session;
import io.github.zhztheplayer.velox4j.memory.AllocationListener;
import io.github.zhztheplayer.velox4j.memory.MemoryManager;
import io.github.zhztheplayer.velox4j.test.Velox4jTests;
import io.github.zhztheplayer.velox4j.type.BooleanType;
import io.github.zhztheplayer.velox4j.type.IntegerType;
import io.github.zhztheplayer.velox4j.type.RealType;
import io.github.zhztheplayer.velox4j.type.RowType;
import io.github.zhztheplayer.velox4j.type.VarCharType;
import io.github.zhztheplayer.velox4j.variant.IntegerValue;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class TypedExprSerdeTest {
  private static MemoryManager memoryManager;

  @BeforeClass
  public static void beforeClass() throws Exception {
    Velox4jTests.ensureInitialized();
    memoryManager = MemoryManager.create(AllocationListener.NOOP);
  }

  @AfterClass
  public static void afterClass() throws Exception {
    memoryManager.close();
  }

  @Test
  public void testCallTypedExpr() {
    SerdeTests.testVeloxSerializableRoundTrip(new CallTypedExpr(new IntegerType(), Collections.emptyList(), "random_int"));
  }

  @Test
  public void testCastTypedExpr() {
    final CallTypedExpr input = new CallTypedExpr(new IntegerType(), Collections.emptyList(), "random_int");
    SerdeTests.testVeloxSerializableRoundTrip(CastTypedExpr.create(new IntegerType(), input, true));
  }

  @Test
  public void testConcatTypedExpr() {
    final CallTypedExpr input1 = new CallTypedExpr(new IntegerType(), Collections.emptyList(), "random_int");
    final CallTypedExpr input2 = new CallTypedExpr(new RealType(), Collections.emptyList(), "random_real");
    SerdeTests.testVeloxSerializableRoundTrip(ConcatTypedExpr.create(List.of("foo", "bar"), List.of(input1, input2)));
  }

  @Test
  public void testConstantTypedExpr() {
    final Session session = Velox4j.newSession(memoryManager);
    final BaseVector intVector = BaseVectorTests.newSampleIntVector(session);
    final ConstantTypedExpr expr1 = ConstantTypedExpr.create(intVector);
    SerdeTests.testVeloxSerializableRoundTrip(expr1);
    final ConstantTypedExpr expr2 = ConstantTypedExpr.create(new IntegerValue(15));
    SerdeTests.testVeloxSerializableRoundTrip(expr2);
    final ConstantTypedExpr expr3 = new ConstantTypedExpr(new IntegerType(), null, BaseVectors.serializeOne(intVector.wrapInConstant(1, 0)));
    SerdeTests.testVeloxSerializableRoundTrip(expr3);
    final ConstantTypedExpr expr4 = new ConstantTypedExpr(new IntegerType(), new IntegerValue(15), null);
    SerdeTests.testVeloxSerializableRoundTrip(expr4);
    session.close();
  }

  @Test
  public void testDereferenceTypedExpr() {
    final CallTypedExpr input1 = new CallTypedExpr(new IntegerType(), Collections.emptyList(), "random_int");
    final CallTypedExpr input2 = new CallTypedExpr(new RealType(), Collections.emptyList(), "random_real");
    final ConcatTypedExpr concat = ConcatTypedExpr.create(List.of("foo", "bar"), List.of(input1, input2));
    final DereferenceTypedExpr dereference = DereferenceTypedExpr.create(concat, 1);
    Assert.assertEquals(RealType.class, dereference.getReturnType().getClass());
    SerdeTests.testVeloxSerializableRoundTrip(dereference);
  }

  @Test
  public void testFieldAccessTypedExpr() {
    final CallTypedExpr input1 = new CallTypedExpr(new IntegerType(), Collections.emptyList(), "random_int");
    final CallTypedExpr input2 = new CallTypedExpr(new RealType(), Collections.emptyList(), "random_real");
    final ConcatTypedExpr concat = ConcatTypedExpr.create(List.of("foo", "bar"), List.of(input1, input2));
    final FieldAccessTypedExpr fieldAccess = FieldAccessTypedExpr.create(concat, "bar");
    Assert.assertEquals(RealType.class, fieldAccess.getReturnType().getClass());
    SerdeTests.testVeloxSerializableRoundTrip(fieldAccess);
  }

  @Test
  public void testInputTypedExpr() {
    SerdeTests.testVeloxSerializableRoundTrip(new InputTypedExpr(new BooleanType()));
  }

  @Test
  public void testLambdaTypedExpr() {
    final RowType signature = new RowType(List.of("foo", "bar"),
        List.of(new IntegerType(), new VarCharType()));
    final LambdaTypedExpr lambdaTypedExpr = LambdaTypedExpr.create(signature,
        FieldAccessTypedExpr.create(new IntegerType(), "foo"));
    SerdeTests.testVeloxSerializableRoundTrip(lambdaTypedExpr);
  }
}

package io.github.zhztheplayer.velox4j.data;

import io.github.zhztheplayer.velox4j.Velox4j;
import io.github.zhztheplayer.velox4j.memory.AllocationListener;
import io.github.zhztheplayer.velox4j.memory.MemoryManager;
import io.github.zhztheplayer.velox4j.serde.Serde;
import io.github.zhztheplayer.velox4j.serde.SerdeTests;
import io.github.zhztheplayer.velox4j.session.Session;
import io.github.zhztheplayer.velox4j.test.ResourceTests;
import io.github.zhztheplayer.velox4j.test.Velox4jTests;
import io.github.zhztheplayer.velox4j.type.IntegerType;
import io.github.zhztheplayer.velox4j.type.RealType;
import io.github.zhztheplayer.velox4j.type.RowType;
import io.github.zhztheplayer.velox4j.type.Type;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class BaseVectorTest {
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
  public void testCreateEmpty1() {
    final Session session = Velox4j.newSession(memoryManager);
    final Type type = new RealType();
    final BaseVector vector = session.baseVectorOps().createEmpty(type);
    Assert.assertEquals(Serde.toPrettyJson(type), Serde.toPrettyJson(vector.getType()));
    Assert.assertEquals(0, vector.getSize());
    session.close();
  }

  @Test
  public void testCreateEmpty2() {
    final Session session = Velox4j.newSession(memoryManager);
    final Type type = new RowType(
        List.of("foo2", "bar2"),
        List.of(new IntegerType(), new IntegerType())
    );
    final BaseVector vector = session.baseVectorOps().createEmpty(type);
    Assert.assertEquals(Serde.toPrettyJson(type), Serde.toPrettyJson(vector.getType()));
    Assert.assertEquals(0, vector.getSize());
    session.close();
  }

  @Test
  public void testToString() {
    final Session session = Velox4j.newSession(memoryManager);
    final RowVector input = BaseVectorTests.newSampleRowVector(session);
    Assert.assertEquals(ResourceTests.readResourceAsString("vector-output/to-string-1.txt"),
        input.toString());
    session.close();
  }

  @Test
  public void testSlice() {
    final Session session = Velox4j.newSession(memoryManager);
    final RowVector input = BaseVectorTests.newSampleRowVector(session);
    Assert.assertEquals(3, input.getSize());
    final RowVector sliced1 = input.slice(0, 2).asRowVector();
    final RowVector sliced2 = input.slice(2, 1).asRowVector();
    Assert.assertEquals(ResourceTests.readResourceAsString("vector-output/slice-1.txt"),
        sliced1.toString());
    Assert.assertEquals(ResourceTests.readResourceAsString("vector-output/slice-2.txt"),
        sliced2.toString());
    session.close();
  }

  @Test
  public void testAppend() {
    final Session session = Velox4j.newSession(memoryManager);
    final RowVector input1 = BaseVectorTests.newSampleRowVector(session);
    final RowVector input2 = BaseVectorTests.newSampleRowVector(session);
    Assert.assertEquals(3, input1.getSize());
    Assert.assertEquals(3, input2.getSize());
    input1.append(input2);
    Assert.assertEquals(6, input1.getSize());
    Assert.assertEquals(3, input2.getSize());
    Assert.assertEquals(ResourceTests.readResourceAsString("vector-output/append-1.txt"),
        input1.toString());
    input2.append(input1);
    Assert.assertEquals(6, input1.getSize());
    Assert.assertEquals(9, input2.getSize());
    Assert.assertEquals(ResourceTests.readResourceAsString("vector-output/append-2.txt"),
        input2.toString());
    session.close();
  }
}

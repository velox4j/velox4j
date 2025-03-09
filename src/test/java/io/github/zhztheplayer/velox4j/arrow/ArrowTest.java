package io.github.zhztheplayer.velox4j.arrow;

import io.github.zhztheplayer.velox4j.Velox4j;
import io.github.zhztheplayer.velox4j.data.BaseVector;
import io.github.zhztheplayer.velox4j.data.BaseVectorTests;
import io.github.zhztheplayer.velox4j.data.RowVector;
import io.github.zhztheplayer.velox4j.memory.AllocationListener;
import io.github.zhztheplayer.velox4j.memory.MemoryManager;
import io.github.zhztheplayer.velox4j.serde.SerdeTests;
import io.github.zhztheplayer.velox4j.session.Session;
import io.github.zhztheplayer.velox4j.test.Velox4jTests;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.table.Table;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ArrowTest {
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
  public void testBaseVectorRoundTrip() {
    final Session session = Velox4j.newSession(memoryManager);
    final RowVector input = BaseVectorTests.newSampleRowVector(session);
    final BufferAllocator alloc = new RootAllocator(Long.MAX_VALUE);
    final FieldVector arrowVector = Arrow.toArrowVector(alloc, input);
    final BaseVector imported = session.arrowOps().fromArrowVector(alloc, arrowVector);
    BaseVectorTests.assertEquals(input, imported);
    arrowVector.close();
    session.close();
  }

  @Test
  public void testRowVectorRoundTrip() {
    final Session session = Velox4j.newSession(memoryManager);
    final RowVector input = BaseVectorTests.newSampleRowVector(session);
    final BufferAllocator alloc = new RootAllocator(Long.MAX_VALUE);
    final Table arrowTable = Arrow.toArrowTable(alloc, input);
    final RowVector imported = session.arrowOps().fromArrowTable(alloc, arrowTable);
    BaseVectorTests.assertEquals(input, imported);
    arrowTable.close();
    session.close();
  }
}

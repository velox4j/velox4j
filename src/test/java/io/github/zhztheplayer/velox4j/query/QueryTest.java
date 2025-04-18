package io.github.zhztheplayer.velox4j.query;

import io.github.zhztheplayer.velox4j.Velox4j;
import io.github.zhztheplayer.velox4j.aggregate.Aggregate;
import io.github.zhztheplayer.velox4j.aggregate.AggregateStep;
import io.github.zhztheplayer.velox4j.collection.Streams;
import io.github.zhztheplayer.velox4j.config.Config;
import io.github.zhztheplayer.velox4j.config.ConnectorConfig;
import io.github.zhztheplayer.velox4j.connector.*;
import io.github.zhztheplayer.velox4j.data.BaseVectorTests;
import io.github.zhztheplayer.velox4j.data.RowVector;
import io.github.zhztheplayer.velox4j.exception.VeloxException;
import io.github.zhztheplayer.velox4j.expression.CallTypedExpr;
import io.github.zhztheplayer.velox4j.expression.ConstantTypedExpr;
import io.github.zhztheplayer.velox4j.expression.FieldAccessTypedExpr;
import io.github.zhztheplayer.velox4j.iterator.DownIterator;
import io.github.zhztheplayer.velox4j.iterator.DownIterators;
import io.github.zhztheplayer.velox4j.iterator.UpIterator;
import io.github.zhztheplayer.velox4j.iterator.UpIterators;
import io.github.zhztheplayer.velox4j.jni.JniWorkspace;
import io.github.zhztheplayer.velox4j.join.JoinType;
import io.github.zhztheplayer.velox4j.memory.AllocationListener;
import io.github.zhztheplayer.velox4j.memory.MemoryManager;
import io.github.zhztheplayer.velox4j.plan.*;
import io.github.zhztheplayer.velox4j.serde.Serde;
import io.github.zhztheplayer.velox4j.session.Session;
import io.github.zhztheplayer.velox4j.sort.SortOrder;
import io.github.zhztheplayer.velox4j.test.ResourceTests;
import io.github.zhztheplayer.velox4j.test.SampleQueryTests;
import io.github.zhztheplayer.velox4j.test.TpchTests;
import io.github.zhztheplayer.velox4j.test.UpIteratorTests;
import io.github.zhztheplayer.velox4j.test.Velox4jTests;
import io.github.zhztheplayer.velox4j.type.BigIntType;
import io.github.zhztheplayer.velox4j.type.BooleanType;
import io.github.zhztheplayer.velox4j.type.RowType;
import io.github.zhztheplayer.velox4j.type.Type;
import io.github.zhztheplayer.velox4j.type.VarCharType;
import io.github.zhztheplayer.velox4j.variant.BigIntValue;
import io.github.zhztheplayer.velox4j.variant.BooleanValue;
import io.github.zhztheplayer.velox4j.write.TableWriteTraits;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class QueryTest {
  public static final String HIVE_CONNECTOR_ID = "connector-hive";
  private static MemoryManager memoryManager;
  private static Session session;

  @BeforeClass
  public static void beforeClass() throws Exception {
    Velox4jTests.ensureInitialized();
    memoryManager = MemoryManager.create(AllocationListener.NOOP);
  }

  @AfterClass
  public static void afterClass() throws Exception {
    memoryManager.close();
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
  public void testValuesWith2Steps() {
    final PlanNode values = ValuesNode.create("id-1",
        List.of(BaseVectorTests.newSampleRowVector(session)), true, 5);
    final Query query = new Query(values, List.of(), Config.empty(), ConnectorConfig.empty());
    final QueryExecutor exec = session.queryOps().createQueryExecutor(query);
    final UpIterator itr = exec.execute();
    SampleQueryTests.assertIterator(itr, 5);
  }

  @Test
  public void testValues() {
    final PlanNode values = ValuesNode.create("id-1",
        List.of(BaseVectorTests.newSampleRowVector(session)), true, 5);
    final Query query = new Query(values, List.of(), Config.empty(), ConnectorConfig.empty());
    final UpIterator itr = session.queryOps().execute(query);
    SampleQueryTests.assertIterator(itr, 5);
  }

  @Test
  public void testTableScan1() {
    final File file = TpchTests.Table.NATION.file();
    final RowType outputType = TpchTests.Table.NATION.schema();
    final TableScanNode scanNode = newSampleTableScanNode("id-1", outputType);
    final List<BoundSplit> splits = List.of(
        newSampleSplit(scanNode, file)
    );
    final Query query = new Query(scanNode, splits, Config.empty(), ConnectorConfig.empty());
    final UpIterator itr = session.queryOps().execute(query);
    UpIteratorTests.assertIterator(itr)
        .assertNumRowVectors(1)
        .assertRowVectorToString(0, ResourceTests.readResourceAsString("query-output/tpch-table-scan-nation.tsv"))
        .run();
  }


  @Test
  public void testTableScan2() {
    final File file = TpchTests.Table.REGION.file();
    final RowType outputType = TpchTests.Table.REGION.schema();
    final TableScanNode scanNode = newSampleTableScanNode("id-1", outputType);
    final List<BoundSplit> splits = List.of(
        newSampleSplit(scanNode, file)
    );
    final Query query = new Query(scanNode, splits, Config.empty(), ConnectorConfig.empty());
    final UpIterator itr = session.queryOps().execute(query);
    UpIteratorTests.assertIterator(itr)
        .assertNumRowVectors(1)
        .assertRowVectorToString(0, ResourceTests.readResourceAsString("query-output/tpch-table-scan-region.tsv"))
        .run();
  }

  @Test
  public void testTableScanCollectMultipleRowVectorsLoadInline() {
    final File file = TpchTests.Table.NATION.file();
    final RowType outputType = TpchTests.Table.NATION.schema();
    final TableScanNode scanNode = newSampleTableScanNode("id-1", outputType);
    final List<BoundSplit> splits = List.of(
        newSampleSplit(scanNode, file)
    );
    final int maxOutputBatchRows = 7;
    final Query query = new Query(scanNode, splits, Config.create(
        Map.of("max_output_batch_rows", String.format("%d", maxOutputBatchRows))),
        ConnectorConfig.empty());
    final UpIterator itr = session.queryOps().execute(query);
    final List<RowVector> allRvs = Streams.fromIterator(UpIterators.asJavaIterator(itr))
        .map(v -> v.loadedVector().asRowVector())
        .collect(Collectors.toList());
    Assert.assertTrue(allRvs.size() > 1);
    for (RowVector rv : allRvs) {
      Assert.assertTrue(rv.getSize() <= maxOutputBatchRows);
    }
    final RowVector appended = session.baseVectorOps().createEmpty(allRvs.get(0).getType()).asRowVector();
    for (RowVector rv : allRvs) {
      appended.append(rv);
    }
    Assert.assertEquals(ResourceTests.readResourceAsString("query-output/tpch-table-scan-nation.tsv"), appended.toString());
  }

  @Test
  public void testTableScanCollectMultipleRowVectorsLoadLast() {
    final File file = TpchTests.Table.NATION.file();
    final RowType outputType = TpchTests.Table.NATION.schema();
    final TableScanNode scanNode = newSampleTableScanNode("id-1", outputType);
    final List<BoundSplit> splits = List.of(
        newSampleSplit(scanNode, file)
    );
    final int maxOutputBatchRows = 7;
    final Query query = new Query(scanNode, splits, Config.create(
        Map.of("max_output_batch_rows", String.format("%d", maxOutputBatchRows))),
        ConnectorConfig.empty());
    final UpIterator itr = session.queryOps().execute(query);
    final List<RowVector> allRvs = Streams.fromIterator(UpIterators.asJavaIterator(itr)).collect(Collectors.toList());
    Assert.assertTrue(allRvs.size() > 1);
    for (int i = 0; i < allRvs.size(); i++) {
      final RowVector rv = allRvs.get(i);
      Assert.assertTrue(rv.getSize() <= maxOutputBatchRows);
      if (i != allRvs.size() - 1) {
        // Vectors except the last one should throw when loading.
        Assert.assertThrows(VeloxException.class, rv::loadedVector);
      } else {
        // The last vector can be loaded without errors.
        rv.loadedVector();
      }
    }
  }

  @Test
  public void testAggregate() {
    final File file = TpchTests.Table.NATION.file();
    final RowType outputType = TpchTests.Table.NATION.schema();
    final TableScanNode scanNode = newSampleTableScanNode("id-1", outputType);
    final List<BoundSplit> splits = List.of(
        newSampleSplit(scanNode, file)
    );
    final AggregationNode aggregationNode = new AggregationNode("id-2", AggregateStep.SINGLE,
        List.of(FieldAccessTypedExpr.create(new BigIntType(), "n_regionkey")),
        List.of(),
        List.of("cnt"),
        List.of(new Aggregate(
            new CallTypedExpr(new BigIntType(), List.of(
                FieldAccessTypedExpr.create(new BigIntType(), "n_nationkey")),
                "sum"),
            List.of(new BigIntType()),
            null,
            List.of(),
            List.of(),
            false
        )),
        false,
        List.of(scanNode),
        null,
        List.of()
    );
    final Query query = new Query(aggregationNode, splits, Config.empty(), ConnectorConfig.empty());
    final UpIterator itr = session.queryOps().execute(query);
    UpIteratorTests.assertIterator(itr)
        .assertNumRowVectors(1)
        .assertRowVectorToString(0, ResourceTests.readResourceAsString("query-output/tpch-aggregate-1.tsv"))
        .run();
  }

  @Test
  public void testExternalStreamFromJavaIterator() {
    final String json = SampleQueryTests.readQueryJson();
    final UpIterator sampleIn = session.queryOps().execute(Serde.fromJson(json, Query.class));
    final DownIterator down = DownIterators.fromJavaIterator(UpIterators.asJavaIterator(sampleIn));
    final ExternalStream es = session.externalStreamOps().bind(down);
    final TableScanNode scanNode = new TableScanNode(
        "id-1",
        SampleQueryTests.getSchema(),
        new ExternalStreamTableHandle("connector-external-stream"),
        List.of()
    );
    final List<BoundSplit> splits = List.of(
        new BoundSplit(
            "id-1",
            -1,
            new ExternalStreamConnectorSplit("connector-external-stream", es.id())
        )
    );
    final Query query = new Query(scanNode, splits, Config.empty(), ConnectorConfig.empty());
    final UpIterator out = session.queryOps().execute(query);
    SampleQueryTests.assertIterator(out);
  }

  @Test
  public void testExternalStreamFromBlockingQueue() throws InterruptedException {
    final BlockingQueue<RowVector> queue = new LinkedBlockingQueue<>();
    final DownIterator down = DownIterators.fromBlockingQueue(queue);
    final ExternalStream es = session.externalStreamOps().bind(down);
    final TableScanNode scanNode = new TableScanNode(
        "id-1",
        SampleQueryTests.getSchema(),
        new ExternalStreamTableHandle("connector-external-stream"),
        List.of()
    );
    final List<BoundSplit> splits = List.of(
        new BoundSplit(
            scanNode.getId(),
            -1,
            new ExternalStreamConnectorSplit("connector-external-stream", es.id())
        )
    );
    final Query query = new Query(scanNode, splits, Config.empty(), ConnectorConfig.empty());
    final UpIterator out = session.queryOps().execute(query);
    final RowVector rv = BaseVectorTests.newSampleRowVector(session);

    // No input added, the up-iterator is considered blocked.
    Assert.assertThrows(VeloxException.class, out::get);
    Assert.assertEquals(UpIterator.State.BLOCKED, out.advance());
    Assert.assertEquals(UpIterator.State.BLOCKED, out.advance());

    // Add one input.
    queue.add(rv);
    out.waitFor();
    Assert.assertEquals(UpIterator.State.AVAILABLE, out.advance());
    Assert.assertThrows(VeloxException.class, out::advance);
    BaseVectorTests.assertEquals(rv, out.get());
    Assert.assertThrows(VeloxException.class, out::get);
    Assert.assertEquals(UpIterator.State.BLOCKED, out.advance());
    Assert.assertEquals(UpIterator.State.BLOCKED, out.advance());

    // Add multiple inputs at a time.
    queue.add(rv);
    queue.add(rv);
    out.waitFor();
    Assert.assertThrows(VeloxException.class, out::waitFor);
    Assert.assertEquals(UpIterator.State.AVAILABLE, out.advance());
    Assert.assertThrows(VeloxException.class, out::advance);
    BaseVectorTests.assertEquals(rv, out.get());
    Assert.assertThrows(VeloxException.class, out::get);
    Assert.assertEquals(UpIterator.State.AVAILABLE, out.advance());
    Assert.assertThrows(VeloxException.class, out::advance);
    BaseVectorTests.assertEquals(rv, out.get());
    Assert.assertThrows(VeloxException.class, out::get);
    Assert.assertEquals(UpIterator.State.BLOCKED, out.advance());
    Assert.assertEquals(UpIterator.State.BLOCKED, out.advance());

  }

  @Test
  public void testExternalStreamFromBlockingQueueTowThreads() throws InterruptedException {
    final BlockingQueue<RowVector> queue = new LinkedBlockingQueue<>();
    final DownIterator down = DownIterators.fromBlockingQueue(queue);
    final ExternalStream es = session.externalStreamOps().bind(down);
    final TableScanNode scanNode = new TableScanNode(
        "id-1",
        SampleQueryTests.getSchema(),
        new ExternalStreamTableHandle("connector-external-stream"),
        List.of()
    );
    final List<BoundSplit> splits = List.of(
        new BoundSplit(
            scanNode.getId(),
            -1,
            new ExternalStreamConnectorSplit("connector-external-stream", es.id())
        )
    );
    final Query query = new Query(scanNode, splits, Config.empty(), ConnectorConfig.empty());
    final UpIterator out = session.queryOps().execute(query);
    final RowVector rv = BaseVectorTests.newSampleRowVector(session);

    final Object control = new Object();

    // No input added, the up-iterator is considered blocked.
    Assert.assertThrows(VeloxException.class, out::get);
    Assert.assertEquals(UpIterator.State.BLOCKED, out.advance());
    Assert.assertEquals(UpIterator.State.BLOCKED, out.advance());

    final Thread testThread = new Thread(() -> {
      try {
        synchronized (control) {
          // Signals the main thread to add one input after 500ms.
          control.notifyAll();
          control.wait();

          // The wait calls should not throw.
          out.waitFor();
          Assert.assertThrows(VeloxException.class, out::waitFor);
          Assert.assertEquals(UpIterator.State.AVAILABLE, out.advance());
          Assert.assertThrows(VeloxException.class, out::advance);
          BaseVectorTests.assertEquals(rv, out.get());
          Assert.assertThrows(VeloxException.class, out::get);
          Assert.assertEquals(UpIterator.State.BLOCKED, out.advance());
          Assert.assertEquals(UpIterator.State.BLOCKED, out.advance());

          // Signals the main thread to add two inputs after 500ms.
          control.notifyAll();
          control.wait();

          // The wait calls should not throw.
          out.waitFor();
          Assert.assertThrows(VeloxException.class, out::waitFor);
          Assert.assertEquals(UpIterator.State.AVAILABLE, out.advance());
          Assert.assertThrows(VeloxException.class, out::advance);
          BaseVectorTests.assertEquals(rv, out.get());
          Assert.assertThrows(VeloxException.class, out::get);
          Assert.assertEquals(UpIterator.State.AVAILABLE, out.advance());
          Assert.assertThrows(VeloxException.class, out::advance);
          BaseVectorTests.assertEquals(rv, out.get());
          Assert.assertThrows(VeloxException.class, out::get);
          Assert.assertEquals(UpIterator.State.BLOCKED, out.advance());
          Assert.assertEquals(UpIterator.State.BLOCKED, out.advance());

          // Signals the main thread that the test has passed.
          control.notifyAll();
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    });

    synchronized (control) {
      // This makes sure test thread starts processing after control.wait().
      testThread.start();
      control.wait();

      // Add one input after 1s.
      new Thread(() -> {
        try {
          Thread.sleep(500L);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        queue.add(rv);
      }).start();


      // Signals the test thread to start checking the output.
      control.notifyAll();
      control.wait();

      // Add two inputs at a time after 1s.
      new Thread(() -> {
        try {
          Thread.sleep(500L);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        queue.add(rv);
        queue.add(rv);
      }).start();

      // Signals the test thread to start checking the output.
      control.notifyAll();
      control.wait();
    }

    testThread.join();
  }

  @Test
  public void testExternalStreamFromBlockingQueueWithInputFiltered() throws InterruptedException {
    final BlockingQueue<RowVector> queue = new LinkedBlockingQueue<>();
    final DownIterator down = DownIterators.fromBlockingQueue(queue);
    final ExternalStream es = session.externalStreamOps().bind(down);
    final TableScanNode scanNode = new TableScanNode(
        "id-1",
        SampleQueryTests.getSchema(),
        new ExternalStreamTableHandle("connector-external-stream"),
        List.of()
    );
    final FilterNode filterNode = new FilterNode(
        "id-2",
        List.of(scanNode),
        ConstantTypedExpr.create(new BooleanValue(false))
    );
    final List<BoundSplit> splits = List.of(
        new BoundSplit(
            scanNode.getId(),
            -1,
            new ExternalStreamConnectorSplit("connector-external-stream", es.id())
        )
    );
    final Query query = new Query(filterNode, splits, Config.empty(), ConnectorConfig.empty());
    final UpIterator out = session.queryOps().execute(query);
    final RowVector rv = BaseVectorTests.newSampleRowVector(session);

    // No input added, the up-iterator is considered blocked.
    Assert.assertThrows(VeloxException.class, out::get);
    Assert.assertEquals(UpIterator.State.BLOCKED, out.advance());
    Assert.assertEquals(UpIterator.State.BLOCKED, out.advance());

    // Add one input.
    queue.add(rv);
    Thread.sleep(500L);
    Assert.assertEquals(UpIterator.State.BLOCKED, out.advance());
    Assert.assertEquals(UpIterator.State.BLOCKED, out.advance());

    // Add multiple inputs at a time.
    queue.add(rv);
    queue.add(rv);
    Thread.sleep(500L);
    Assert.assertEquals(UpIterator.State.BLOCKED, out.advance());
    Assert.assertEquals(UpIterator.State.BLOCKED, out.advance());

  }

  @Test
  public void testProject() {
    final File file = TpchTests.Table.NATION.file();
    final RowType outputType = TpchTests.Table.NATION.schema();
    final TableScanNode scanNode = newSampleTableScanNode("id-1", outputType);
    final List<BoundSplit> splits = List.of(
        newSampleSplit(scanNode, file)
    );
    final ProjectNode projectNode = new ProjectNode("id-2", List.of(scanNode),
        List.of("n_nationkey", "n_comment"),
        List.of(
            FieldAccessTypedExpr.create(new BigIntType(), "n_nationkey"),
            FieldAccessTypedExpr.create(new VarCharType(), "n_comment")
        ));
    final Query query = new Query(projectNode, splits, Config.empty(), ConnectorConfig.empty());
    final UpIterator itr = session.queryOps().execute(query);
    UpIteratorTests.assertIterator(itr)
        .assertNumRowVectors(1)
        .assertRowVectorToString(0, ResourceTests.readResourceAsString("query-output/tpch-project-1.tsv"))
        .run();
  }

  @Test
  public void testFilter() {
    final File file = TpchTests.Table.NATION.file();
    final RowType outputType = TpchTests.Table.NATION.schema();
    final TableScanNode scanNode = newSampleTableScanNode("id-1", outputType);
    final List<BoundSplit> splits = List.of(
        newSampleSplit(scanNode, file)
    );
    final FilterNode filterNode = new FilterNode("id-2", List.of(scanNode),
        new CallTypedExpr(new BooleanType(), List.of(
            FieldAccessTypedExpr.create(new BigIntType(), "n_regionkey"),
            ConstantTypedExpr.create(new BigIntValue(3))),
            "greaterthanorequal"));
    final Query query = new Query(filterNode, splits, Config.empty(), ConnectorConfig.empty());
    final UpIterator itr = session.queryOps().execute(query);
    UpIteratorTests.assertIterator(itr)
        .assertNumRowVectors(1)
        .assertRowVectorToString(0, ResourceTests.readResourceAsString("query-output/tpch-filter-1.tsv"))
        .run();
  }

  @Test
  public void testHashJoin() {
    final File nationFile = TpchTests.Table.NATION.file();
    final RowType nationOutputType = TpchTests.Table.NATION.schema();
    final File regionFile = TpchTests.Table.REGION.file();
    final RowType regionOutputType = TpchTests.Table.REGION.schema();
    final TableScanNode nationScanNode = newSampleTableScanNode("id-1", nationOutputType);
    final TableScanNode regionScanNode = newSampleTableScanNode("id-2", regionOutputType);
    final List<BoundSplit> splits = List.of(
        newSampleSplit(nationScanNode, nationFile),
        newSampleSplit(regionScanNode, regionFile)
    );
    final HashJoinNode hashJoinNode = new HashJoinNode("id-3",
        JoinType.LEFT,
        List.of(FieldAccessTypedExpr.create(new BigIntType(), "n_regionkey")),
        List.of(FieldAccessTypedExpr.create(new BigIntType(), "r_regionkey")),
        null,
        nationScanNode,
        regionScanNode,
        new RowType(List.of("n_nationkey", "n_name", "r_regionkey", "r_name"),
            List.of(new BigIntType(), new VarCharType(), new BigIntType(), new VarCharType())),
        false
    );
    final Query query = new Query(hashJoinNode, splits, Config.empty(), ConnectorConfig.empty());
    final UpIterator itr = session.queryOps().execute(query);
    UpIteratorTests.assertIterator(itr)
        .assertNumRowVectors(1)
        .assertRowVectorToString(0, ResourceTests.readResourceAsString("query-output/tpch-join-1.tsv"))
        .run();
  }

  @Test
  public void testOrderBy() {
    final File file = TpchTests.Table.NATION.file();
    final RowType outputType = TpchTests.Table.NATION.schema();
    final TableScanNode scanNode = newSampleTableScanNode("id-1", outputType);
    final List<BoundSplit> splits = List.of(
        newSampleSplit(scanNode, file)
    );
    final OrderByNode orderByNode = new OrderByNode("id-2", List.of(scanNode),
        List.of(FieldAccessTypedExpr.create(new BigIntType(), "n_regionkey"),
            FieldAccessTypedExpr.create(new BigIntType(), "n_nationkey")),
        List.of(new SortOrder(true, false),
            new SortOrder(false, false)),
        false);
    final Query query = new Query(orderByNode, splits, Config.empty(), ConnectorConfig.empty());
    final UpIterator itr = session.queryOps().execute(query);
    UpIteratorTests.assertIterator(itr)
        .assertNumRowVectors(1)
        .assertRowVectorToString(0, ResourceTests.readResourceAsString("query-output/tpch-orderby-1.tsv"))
        .run();
  }

  @Test
  public void testLimit() {
    final File file = TpchTests.Table.NATION.file();
    final RowType outputType = TpchTests.Table.NATION.schema();
    final TableScanNode scanNode = newSampleTableScanNode("id-1", outputType);
    final List<BoundSplit> splits = List.of(
        newSampleSplit(scanNode, file)
    );
    final LimitNode limitNode = new LimitNode("id-2", List.of(scanNode), 5, 3, false);
    final Query query = new Query(limitNode, splits, Config.empty(), ConnectorConfig.empty());
    final UpIterator itr = session.queryOps().execute(query);
    UpIteratorTests.assertIterator(itr)
        .assertNumRowVectors(1)
        .assertRowVectorToString(0, ResourceTests.readResourceAsString("query-output/tpch-limit-1.tsv"))
        .run();
  }

  @Test
  public void testTableWrite() throws IOException {
    final File folder = JniWorkspace.getDefault().getSubDir("test");
    final String fileName = String.format("test-write-%s.tmp", UUID.randomUUID());
    final File file = TpchTests.Table.NATION.file();
    final RowType schema = TpchTests.Table.NATION.schema();
    final TableScanNode scanNode = newSampleTableScanNode("id-1", schema);
    final List<BoundSplit> splits = List.of(
        newSampleSplit(scanNode, file)
    );
    final TableWriteNode tableWriteNode = newSampleTableWriteNode("id-2", schema, folder, fileName, scanNode);
    final Query query = new Query(tableWriteNode, splits, Config.empty(), ConnectorConfig.empty());
    final UpIterator itr = session.queryOps().execute(query);
    UpIteratorTests.assertIterator(itr)
        .assertNumRowVectors(1)
        .assertRowVectorTypeJson(0, ResourceTests.readResourceAsString("query-output-type/tpch-table-write-1.json"))
        .run();
  }

  @Test
  public void testTableWriteRoundTrip() throws IOException {
    final File folder = JniWorkspace.getDefault().getSubDir("test");
    final String fileName = String.format("test-write-%s.tmp", UUID.randomUUID());
    final RowType schema = TpchTests.Table.NATION.schema();

    // Read the sample nation file.
    final File file = TpchTests.Table.NATION.file();
    final TableScanNode scanNode1 = newSampleTableScanNode("id-1", schema);
    final List<BoundSplit> splits1 = List.of(
        newSampleSplit(scanNode1, file)
    );
    final TableWriteNode tableWriteNode = newSampleTableWriteNode("id-2", schema, folder, fileName, scanNode1);
    final Query query1 = new Query(tableWriteNode, splits1, Config.empty(), ConnectorConfig.empty());
    final UpIterator itr1 = session.queryOps().execute(query1);
    UpIteratorTests.assertIterator(itr1)
        .assertNumRowVectors(1)
        .assertRowVectorTypeJson(0, ResourceTests.readResourceAsString("query-output-type/tpch-table-write-1.json"))
        .run();

    // Read the file we just wrote.
    final File writtenFile = folder.toPath().resolve(fileName).toFile();
    ;
    final TableScanNode scanNode2 = newSampleTableScanNode("id-1", schema);
    final List<BoundSplit> splits2 = List.of(
        newSampleSplit(scanNode2, writtenFile)
    );
    final Query query2 = new Query(scanNode2, splits2, Config.empty(), ConnectorConfig.empty());
    final UpIterator itr2 = session.queryOps().execute(query2);
    UpIteratorTests.assertIterator(itr2)
        .assertNumRowVectors(1)
        .assertRowVectorToString(0, ResourceTests.readResourceAsString("query-output/tpch-table-scan-nation.tsv"))
        .run();

  }

  private static TableWriteNode newSampleTableWriteNode(String id, RowType schema, File folder, String fileName, TableScanNode scanNode) {
    final ConnectorInsertTableHandle handle = new HiveInsertTableHandle(
        toColumnHandles(schema),
        new LocationHandle(
            folder.getAbsolutePath(),
            folder.getAbsolutePath(),
            LocationHandle.TableType.NEW,
            fileName
        ),
        FileFormat.PARQUET,
        null,
        CompressionKind.GZIP,
        Map.of(),
        true,
        new HiveInsertFileNameGenerator()
    );
    final RowType outputType = TableWriteTraits.outputType();
    final TableWriteNode tableWriteNode = new TableWriteNode(
        id,
        schema,
        schema.getNames(),
        null,
        HIVE_CONNECTOR_ID,
        handle,
        false,
        outputType,
        CommitStrategy.NO_COMMIT,
        List.of(scanNode)
    );
    return tableWriteNode;
  }

  private static List<Assignment> toAssignments(RowType rowType) {
    final List<Assignment> list = new ArrayList<>();
    for (int i = 0; i < rowType.size(); i++) {
      final String name = rowType.getNames().get(i);
      final Type type = rowType.getChildren().get(i);
      list.add(new Assignment(name,
          new HiveColumnHandle(name, ColumnType.REGULAR, type, type, List.of())));
    }
    return list;
  }

  private static List<HiveColumnHandle> toColumnHandles(RowType rowType) {
    final List<HiveColumnHandle> list = new ArrayList<>();
    for (int i = 0; i < rowType.size(); i++) {
      final String name = rowType.getNames().get(i);
      final Type type = rowType.getChildren().get(i);
      list.add(new HiveColumnHandle(name, ColumnType.REGULAR, type, type, List.of()));
    }
    return list;
  }

  private static BoundSplit newSampleSplit(TableScanNode scanNode, File file) {
    return new BoundSplit(
        scanNode.getId(),
        -1,
        new HiveConnectorSplit(
            "connector-hive",
            0,
            false,
            file.getAbsolutePath(),
            FileFormat.PARQUET,
            0,
            file.length(),
            Map.of(),
            OptionalInt.empty(),
            Optional.empty(),
            Map.of(),
            Optional.empty(),
            Map.of(),
            Map.of(),
            Map.of(),
            Optional.empty(),
            Optional.empty()
        )
    );
  }

  private static TableScanNode newSampleTableScanNode(String planNodeId, RowType outputType) {
    final TableScanNode scanNode = new TableScanNode(
        planNodeId,
        outputType,
        new HiveTableHandle(
            "connector-hive",
            "tab-1",
            false,
            List.of(),
            null,
            outputType,
            Map.of()
        ),
        toAssignments(outputType)
    );
    return scanNode;
  }
}

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
package io.github.zhztheplayer.velox4j.serializable;

import io.github.zhztheplayer.velox4j.config.Config;
import io.github.zhztheplayer.velox4j.config.ConnectorConfig;
import io.github.zhztheplayer.velox4j.connector.*;
import io.github.zhztheplayer.velox4j.eval.Evaluation;
import io.github.zhztheplayer.velox4j.expression.CallTypedExpr;
import io.github.zhztheplayer.velox4j.expression.CastTypedExpr;
import io.github.zhztheplayer.velox4j.expression.ConcatTypedExpr;
import io.github.zhztheplayer.velox4j.expression.ConstantTypedExpr;
import io.github.zhztheplayer.velox4j.expression.DereferenceTypedExpr;
import io.github.zhztheplayer.velox4j.expression.FieldAccessTypedExpr;
import io.github.zhztheplayer.velox4j.expression.InputTypedExpr;
import io.github.zhztheplayer.velox4j.expression.LambdaTypedExpr;
import io.github.zhztheplayer.velox4j.filter.AlwaysTrue;
import io.github.zhztheplayer.velox4j.plan.AggregationNode;
import io.github.zhztheplayer.velox4j.plan.FilterNode;
import io.github.zhztheplayer.velox4j.plan.HashJoinNode;
import io.github.zhztheplayer.velox4j.plan.LimitNode;
import io.github.zhztheplayer.velox4j.plan.OrderByNode;
import io.github.zhztheplayer.velox4j.plan.ProjectNode;
import io.github.zhztheplayer.velox4j.plan.TableScanNode;
import io.github.zhztheplayer.velox4j.plan.TableWriteNode;
import io.github.zhztheplayer.velox4j.plan.ValuesNode;
import io.github.zhztheplayer.velox4j.plan.WindowNode;
import io.github.zhztheplayer.velox4j.query.Query;
import io.github.zhztheplayer.velox4j.serde.Serde;
import io.github.zhztheplayer.velox4j.serde.SerdeRegistry;
import io.github.zhztheplayer.velox4j.serde.SerdeRegistryFactory;
import io.github.zhztheplayer.velox4j.type.ArrayType;
import io.github.zhztheplayer.velox4j.type.BigIntType;
import io.github.zhztheplayer.velox4j.type.BooleanType;
import io.github.zhztheplayer.velox4j.type.DateType;
import io.github.zhztheplayer.velox4j.type.DecimalType;
import io.github.zhztheplayer.velox4j.type.DoubleType;
import io.github.zhztheplayer.velox4j.type.FunctionType;
import io.github.zhztheplayer.velox4j.type.HugeIntType;
import io.github.zhztheplayer.velox4j.type.IntegerType;
import io.github.zhztheplayer.velox4j.type.IntervalDayTimeType;
import io.github.zhztheplayer.velox4j.type.IntervalYearMonthType;
import io.github.zhztheplayer.velox4j.type.MapType;
import io.github.zhztheplayer.velox4j.type.OpaqueType;
import io.github.zhztheplayer.velox4j.type.RealType;
import io.github.zhztheplayer.velox4j.type.RowType;
import io.github.zhztheplayer.velox4j.type.SmallIntType;
import io.github.zhztheplayer.velox4j.type.TimestampType;
import io.github.zhztheplayer.velox4j.type.TinyIntType;
import io.github.zhztheplayer.velox4j.type.UnknownType;
import io.github.zhztheplayer.velox4j.type.VarCharType;
import io.github.zhztheplayer.velox4j.type.VarbinaryType;
import io.github.zhztheplayer.velox4j.window.WindowFrame;
import io.github.zhztheplayer.velox4j.window.WindowFunction;

public final class ISerializableRegistry {
  private static final SerdeRegistry NAME_REGISTRY =
      SerdeRegistryFactory.createForBaseClass(ISerializable.class).key("name");

  private ISerializableRegistry() {}

  public static void registerAll() {
    Serde.registerBaseClass(ISerializable.class);
    registerTypes();
    registerExprs();
    registerConnectors();
    registerFilters();
    registerPlanNodes();
    registerWindow();
    registerConfig();
    registerEvaluation();
    registerQuery();
  }

  private static void registerTypes() {
    final SerdeRegistry typeRegistry = NAME_REGISTRY.registerFactory("Type").key("type");
    typeRegistry.registerClass("BOOLEAN", BooleanType.class);
    typeRegistry.registerClass("TINYINT", TinyIntType.class);
    typeRegistry.registerClass("SMALLINT", SmallIntType.class);
    typeRegistry.registerClass("INTEGER", IntegerType.class);
    typeRegistry.registerClass("BIGINT", BigIntType.class);
    typeRegistry.registerClass("HUGEINT", HugeIntType.class);
    typeRegistry.registerClass("REAL", RealType.class);
    typeRegistry.registerClass("DOUBLE", DoubleType.class);
    typeRegistry.registerClass("VARCHAR", VarCharType.class);
    typeRegistry.registerClass("VARBINARY", VarbinaryType.class);
    typeRegistry.registerClass("TIMESTAMP", TimestampType.class);
    typeRegistry.registerClass("ARRAY", ArrayType.class);
    typeRegistry.registerClass("MAP", MapType.class);
    typeRegistry.registerClass("ROW", RowType.class);
    typeRegistry.registerClass("FUNCTION", FunctionType.class);
    typeRegistry.registerClass("UNKNOWN", UnknownType.class);
    typeRegistry.registerClass("OPAQUE", OpaqueType.class);
    typeRegistry.registerClass("DECIMAL", DecimalType.class);
    NAME_REGISTRY
        .registerFactory("IntervalDayTimeType")
        .key("type")
        .registerClass("INTERVAL DAY TO SECOND", IntervalDayTimeType.class);
    NAME_REGISTRY
        .registerFactory("IntervalYearMonthType")
        .key("type")
        .registerClass("INTERVAL YEAR TO MONTH", IntervalYearMonthType.class);
    NAME_REGISTRY.registerFactory("DateType").key("type").registerClass("DATE", DateType.class);
  }

  private static void registerExprs() {
    NAME_REGISTRY.registerClass("CallTypedExpr", CallTypedExpr.class);
    NAME_REGISTRY.registerClass("CastTypedExpr", CastTypedExpr.class);
    NAME_REGISTRY.registerClass("ConcatTypedExpr", ConcatTypedExpr.class);
    NAME_REGISTRY.registerClass("ConstantTypedExpr", ConstantTypedExpr.class);
    NAME_REGISTRY.registerClass("DereferenceTypedExpr", DereferenceTypedExpr.class);
    NAME_REGISTRY.registerClass("FieldAccessTypedExpr", FieldAccessTypedExpr.class);
    NAME_REGISTRY.registerClass("InputTypedExpr", InputTypedExpr.class);
    NAME_REGISTRY.registerClass("LambdaTypedExpr", LambdaTypedExpr.class);
  }

  private static void registerConnectors() {
    NAME_REGISTRY.registerClass("HiveColumnHandle", HiveColumnHandle.class);
    NAME_REGISTRY.registerClass("HiveConnectorSplit", HiveConnectorSplit.class);
    NAME_REGISTRY.registerClass("HiveTableHandle", HiveTableHandle.class);
    NAME_REGISTRY.registerClass("ExternalStreamConnectorSplit", ExternalStreamConnectorSplit.class);
    NAME_REGISTRY.registerClass("ExternalStreamTableHandle", ExternalStreamTableHandle.class);
    NAME_REGISTRY.registerClass("LocationHandle", LocationHandle.class);
    NAME_REGISTRY.registerClass("HiveSortingColumn", HiveSortingColumn.class);
    NAME_REGISTRY.registerClass("HiveBucketProperty", HiveBucketProperty.class);
    NAME_REGISTRY.registerClass("HiveInsertTableHandle", HiveInsertTableHandle.class);
    NAME_REGISTRY.registerClass("HiveInsertFileNameGenerator", HiveInsertFileNameGenerator.class);
  }

  private static void registerFilters() {
    NAME_REGISTRY.registerClass("AlwaysTrue", AlwaysTrue.class);
  }

  private static void registerPlanNodes() {
    NAME_REGISTRY.registerClass("ValuesNode", ValuesNode.class);
    NAME_REGISTRY.registerClass("TableScanNode", TableScanNode.class);
    NAME_REGISTRY.registerClass("AggregationNode", AggregationNode.class);
    NAME_REGISTRY.registerClass("ProjectNode", ProjectNode.class);
    NAME_REGISTRY.registerClass("FilterNode", FilterNode.class);
    NAME_REGISTRY.registerClass("HashJoinNode", HashJoinNode.class);
    NAME_REGISTRY.registerClass("OrderByNode", OrderByNode.class);
    NAME_REGISTRY.registerClass("LimitNode", LimitNode.class);
    NAME_REGISTRY.registerClass("TableWriteNode", TableWriteNode.class);
    NAME_REGISTRY.registerClass("WindowNode", WindowNode.class);
  }

  private static void registerWindow() {
    NAME_REGISTRY.registerClass("Frame", WindowFrame.class);
    NAME_REGISTRY.registerClass("Function", WindowFunction.class);
  }

  private static void registerConfig() {
    NAME_REGISTRY.registerClass("velox4j.Config", Config.class);
    NAME_REGISTRY.registerClass("velox4j.ConnectorConfig", ConnectorConfig.class);
  }

  private static void registerEvaluation() {
    NAME_REGISTRY.registerClass("velox4j.Evaluation", Evaluation.class);
  }

  private static void registerQuery() {
    NAME_REGISTRY.registerClass("velox4j.Query", Query.class);
  }
}

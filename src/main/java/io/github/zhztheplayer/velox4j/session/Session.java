package io.github.zhztheplayer.velox4j.session;

import io.github.zhztheplayer.velox4j.arrow.Arrow;
import io.github.zhztheplayer.velox4j.connector.ExternalStreams;
import io.github.zhztheplayer.velox4j.data.BaseVectors;
import io.github.zhztheplayer.velox4j.data.RowVectors;
import io.github.zhztheplayer.velox4j.data.SelectivityVectors;
import io.github.zhztheplayer.velox4j.expression.Expressions;
import io.github.zhztheplayer.velox4j.jni.CppObject;
import io.github.zhztheplayer.velox4j.query.Queries;

public interface Session extends CppObject {
  Expressions expressionOps();

  Queries queryOps();

  ExternalStreams externalStreamOps();

  BaseVectors baseVectorOps();

  RowVectors rowVectorOps();

  SelectivityVectors selectivityVectorOps();

  Arrow arrowOps();
}

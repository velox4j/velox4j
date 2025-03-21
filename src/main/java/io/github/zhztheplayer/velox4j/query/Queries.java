package io.github.zhztheplayer.velox4j.query;

import io.github.zhztheplayer.velox4j.iterator.UpIterator;
import io.github.zhztheplayer.velox4j.jni.JniApi;

public class Queries {
  private final JniApi jniApi;

  public Queries(JniApi jniApi) {
    this.jniApi = jniApi;
  }

  public UpIterator execute(Query query) {
    return jniApi.executeQuery(query);
  }
}

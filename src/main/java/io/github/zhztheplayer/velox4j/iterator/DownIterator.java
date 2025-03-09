package io.github.zhztheplayer.velox4j.iterator;

import io.github.zhztheplayer.velox4j.jni.CalledFromNative;

public interface DownIterator {
  enum State {
    AVAILABLE(0),
    BLOCKED(1),
    FINISHED(2);

    private final int id;

    State(int id) {
      this.id = id;
    }

    public int getId() {
      return id;
    }
  }

  @CalledFromNative
  default int advance() {
      return advance0().getId();
  }
  @CalledFromNative
  long get();
  @CalledFromNative
  void close();

  State advance0();
}

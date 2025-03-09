package io.github.zhztheplayer.velox4j.iterator;

import com.google.common.base.Preconditions;
import io.github.zhztheplayer.velox4j.data.RowVector;
import io.github.zhztheplayer.velox4j.jni.JniApi;
import io.github.zhztheplayer.velox4j.jni.CppObject;
import io.github.zhztheplayer.velox4j.jni.StaticJniApi;

import java.util.HashMap;
import java.util.Map;

public class UpIterator implements CppObject {
  private static final Map<Integer, State> ID_LOOKUP = new HashMap<>();

  public enum State {
    AVAILABLE(0),
    BLOCKED(1),
    FINISHED(2);

    public static State get(int id) {
      Preconditions.checkArgument(ID_LOOKUP.containsKey(id), "ID not found: %d", id);
      return ID_LOOKUP.get(id);
    }

    private final int id;

    State(int id) {
      this.id = id;
      Preconditions.checkArgument(!ID_LOOKUP.containsKey(id));
      ID_LOOKUP.put(id, this);
    }

    public int getId() {
      return id;
    }
  }

  private final JniApi jniApi;
  private final long id;

  public UpIterator(JniApi jniApi, long id) {
    this.jniApi = jniApi;
    this.id = id;
  }

  public State advance() {
    return StaticJniApi.get().upIteratorAdvance(this);
  }

  public RowVector get() {
    return jniApi.upIteratorGet(this);
  }

  @Override
  public long id() {
    return id;
  }
}

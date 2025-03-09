package io.github.zhztheplayer.velox4j.iterator;

import java.util.Iterator;

public interface CloseableIterator<T> extends Iterator<T>, AutoCloseable {
}

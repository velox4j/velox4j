package io.github.zhztheplayer.velox4j.iterator;

import io.github.zhztheplayer.velox4j.data.RowVector;

public final class UpIterators {
  public static CloseableIterator<RowVector> asJavaIterator(UpIterator upIterator) {
    return new AsJavaIterator(upIterator);
  }

  private static class AsJavaIterator implements CloseableIterator<RowVector> {
    private final UpIterator upIterator;

    private AsJavaIterator(UpIterator upIterator) {
      this.upIterator = upIterator;
    }

    @Override
    public boolean hasNext() {
      while (true) {
        final UpIterator.State state = upIterator.advance();
        switch (state) {
          case BLOCKED:
            continue;
          case AVAILABLE:
            return true;
          case FINISHED:
            return false;
        }
      }
    }

    @Override
    public RowVector next() {
      return upIterator.get();
    }

    @Override
    public void close() throws Exception {
      upIterator.close();
    }
  }
}

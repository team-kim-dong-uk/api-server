package pics.udhd.query.util.bktree;

public interface Metric<E> {

  /**
   * Returns the distance between the given elements.
   */
  int distance(E x, E y);
}

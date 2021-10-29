package com.udhd.apiserver.util.bktree;

import static java.lang.String.format;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.springframework.lang.Nullable;
import com.udhd.apiserver.util.bktree.Exception.IllegalMetricException;

/**
 * A mutable {@linkplain edu.gatech.gtri.bktree.BkTree BK-tree}.
 *
 * <p>Mutating operations are <em>not</em> thread-safe.
 *
 * <p>Whereas the {@linkplain #add(Object) mutating methods} are iterative and
 * can thus handle very large trees, the {@link #equals(Object)},
 * {@link #hashCode()} and {@link #toString()} methods on this class and its
 * {@link edu.gatech.gtri.bktree.BkTree.Node} implementation are each recursive and as such may not
 * complete normally when called on very deep trees.
 *
 * @param <E> type of elements in this tree
 */
public final class MutableBkTree<E> implements BkTree<E> {

  private final Metric<? super E> metric;
  @Nullable
  MutableNode<E> root;

  public MutableBkTree(Metric<? super E> metric) {
    if (metric == null) throw new NullPointerException();
    this.metric = metric;
  }

  /**
   * Adds the given element to this tree, if it's not already present.
   *
   * @param element element
   */
  public void add(E element) {
    if (element == null) throw new NullPointerException();

    if (root == null) {
      root = new MutableNode<>(element);
    } else {
      MutableNode<E> node = root;
      while (!node.getElement().equals(element)) {
        int distance = distance(node.getElement(), element);

        MutableNode<E> parent = node;
        node = parent.childrenByDistance.get(distance);
        if (node == null) {
          node = new MutableNode<>(element);
          parent.childrenByDistance.put(distance, node);
          break;
        }
      }
    }
  }

  private int distance(E x, E y) {
    int distance = metric.distance(x, y);
    if (distance < 0) {
      throw new IllegalMetricException(
          format("negative distance (%d) defined between elements `%s` and `%s`", distance, x, y));
    }
    return distance;
  }

  /**
   * Adds all of the given elements to this tree.
   *
   * @param elements elements
   */
  public void addAll(Iterable<? extends E> elements) {
    if (elements == null) throw new NullPointerException();
    for (E element : elements) {
      add(element);
    }
  }

  /**
   * Adds all of the given elements to this tree.
   *
   * @param elements elements
   */
  @SafeVarargs
  public final void addAll(E... elements) {
    if (elements == null) throw new NullPointerException();
    addAll(Arrays.asList(elements));
  }

  @Override
  public Metric<? super E> getMetric() {
    return metric;
  }

  @Override
  public @Nullable Node<E> getRoot() {
    return root;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MutableBkTree that = (MutableBkTree) o;

    if (!metric.equals(that.metric)) return false;
    if (root != null ? !root.equals(that.root) : that.root != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = metric.hashCode();
    result = 31 * result + (root != null ? root.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("MutableBkTree{");
    sb.append("metric=").append(metric);
    sb.append(", root=").append(root);
    sb.append('}');
    return sb.toString();
  }

  static final class MutableNode<E> implements Node<E> {
    final E element;
    final Map<Integer, MutableNode<E>> childrenByDistance = new HashMap<>();

    MutableNode(E element) {
      if (element == null) throw new NullPointerException();
      this.element = element;
    }

    @Override
    public E getElement() {
      return element;
    }

    @Override
    public @Nullable Node<E> getChildNode(int distance) {
      return childrenByDistance.get(distance);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      MutableNode that = (MutableNode) o;

      if (!childrenByDistance.equals(that.childrenByDistance)) return false;
      if (!element.equals(that.element)) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = element.hashCode();
      result = 31 * result + childrenByDistance.hashCode();
      return result;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("MutableNode{");
      sb.append("element=").append(element);
      sb.append(", childrenByDistance=").append(childrenByDistance);
      sb.append('}');
      return sb.toString();
    }
  }
}

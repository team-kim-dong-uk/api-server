package com.udhd.apiserver.util.bktree;

import static java.lang.Math.max;
import static java.lang.String.format;

import com.udhd.apiserver.util.bktree.BkTree.Node;
import com.udhd.apiserver.util.bktree.Exception.IllegalMetricException;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * Searches a {@link BkTree}.
 *
 * @param <E> type of elements in the searched tree
 */
public class BkTreeSearcher<E> {

  private final BkTree<E> tree;

  /**
   * Constructs a searcher that orders matches in increasing order of distance from the query.
   *
   * @param tree tree to search
   */
  public BkTreeSearcher(BkTree<E> tree) {
    if (tree == null) {
      throw new NullPointerException();
    }
    this.tree = tree;
  }

  public SearchResult<? extends E> search(E query, SearchOption option) {
    int minDistance = option.getMinDistance() == null ? Integer.MIN_VALUE : option.getMinDistance();
    int maxDistance = option.getMaxDistance() == null ? Integer.MAX_VALUE : option.getMaxDistance();
    int limit = option.getLimit() == null ? Integer.MAX_VALUE : option.getLimit();
    int lastDistance = minDistance;

    if (maxDistance == Integer.MAX_VALUE && limit == Integer.MAX_VALUE) {
      throw new IllegalArgumentException(
          "Either maxDistance and limit must not be the default value");
    }
    if (query == null) {
      throw new NullPointerException();
    }
    if (maxDistance < 0) {
      throw new IllegalArgumentException("maxDistance must be non-negative");
    }

    Metric<? super E> metric = tree.getMetric();

    Set<Match<?>> matches = new HashSet<>();

    Queue<Node<E>> queue = new ArrayDeque<>();
    queue.add(tree.getRoot());

    while (!queue.isEmpty()) {
      Node<E> node = queue.remove();
      E element = node.getElement();

      int distance = metric.distance(element, query);
      if (distance < 0) {
        throw new IllegalMetricException(
            format("negative distance (%d) defined between element `%s` and query `%s`",
                distance, element, query));
      }

      if (distance >= minDistance && distance <= maxDistance) {
        matches.add(new Match<>(element, distance));
        lastDistance = Math.max(distance, lastDistance);
        if (matches.size() >= limit) {
          break;
        }
      }

      int minSearchDistance = max(distance - maxDistance, 0);
      int maxSearchDistance = distance + maxDistance;

      for (int searchDistance = minSearchDistance; searchDistance <= maxSearchDistance;
          ++searchDistance) {
        Node<E> childNode = node.getChildNode(searchDistance);
        if (childNode != null) {
          queue.add(childNode);
        }
      }
    }

    return (SearchResult<? extends E>) SearchResult.builder()
        .matches(matches)
        .lastDistance(lastDistance)
        .build();
  }

  /**
   * Returns the tree searched by this searcher.
   */
  public BkTree<E> getTree() {
    return tree;
  }

  /**
   * An element matching a query.
   *
   * @param <E> type of matching element
   */
  public static final class Match<E> {

    private final E match;
    private final int distance;

    /**
     * @param match matching element
     * @param distance distance of the matching element from the search query
     */
    public Match(E match, int distance) {
      if (match == null) {
        throw new NullPointerException();
      }
      if (distance < 0) {
        throw new IllegalArgumentException("distance must be non-negative");
      }

      this.match = match;
      this.distance = distance;
    }

    /**
     * Returns the matching element.
     */
    public E getMatch() {
      return match;
    }

    /**
     * Returns the matching element's distance from the search query.
     */
    public int getDistance() {
      return distance;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      Match that = (Match) o;

      if (distance != that.distance) {
        return false;
      }
      if (!match.equals(that.match)) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      int result = match.hashCode();
      result = 31 * result + distance;
      return result;
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder("Match{");
      sb.append("match=").append(match);
      sb.append(", distance=").append(distance);
      sb.append('}');
      return sb.toString();
    }
  }
}


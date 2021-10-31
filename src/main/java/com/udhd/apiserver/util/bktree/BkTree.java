package com.udhd.apiserver.util.bktree;

import org.springframework.lang.Nullable;

public interface BkTree<E> {

  Metric<? super E> getMetric();

  Node<E> getRoot();

  interface Node<E> {

    E getElement();

    @Nullable
    Node<E> getChildNode(int distance);
  }
}

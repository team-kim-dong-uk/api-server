package com.udhd.apiserver.util.bktree;

import java.util.Set;
import lombok.Builder;
import lombok.Data;
import com.udhd.apiserver.util.bktree.BkTreeSearcher.Match;

@Data
@Builder
public class SearchResult<E> {

  Set<Match<? extends E>> matches;
  int lastDistance;
}

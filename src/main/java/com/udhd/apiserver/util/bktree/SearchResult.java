package com.udhd.apiserver.util.bktree;

import com.udhd.apiserver.util.bktree.BkTreeSearcher.Match;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchResult<E> {

  Set<Match<? extends E>> matches;
  int lastDistance;
}

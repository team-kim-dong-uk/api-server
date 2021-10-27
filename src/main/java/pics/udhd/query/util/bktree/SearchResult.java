package pics.udhd.query.util.bktree;

import java.util.Set;
import lombok.Builder;
import lombok.Data;
import pics.udhd.query.util.bktree.BkTreeSearcher.Match;

@Data
@Builder
public class SearchResult<E> {

  Set<Match<? extends E>> matches;
  int lastDistance;
}

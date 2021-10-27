package pics.udhd.query.util.bktree;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchOption {

  Integer minDistance;
  Integer maxDistance;
  Integer limit;

}

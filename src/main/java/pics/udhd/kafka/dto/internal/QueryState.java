package pics.udhd.kafka.dto.internal;

import java.util.List;
import java.util.UUID;
import pics.udhd.kafka.QueryCallback;
import pics.udhd.kafka.dto.PhotoDto;
import pics.udhd.kafka.dto.QueryResultDto;

public class QueryState {

  public UUID queryKey;
  public QueryResultDto result;
  public List<PhotoDto> query;
  public QueryCallback callback;

  public void accumulateResult(ResultDto resultDto) {
    result.accumulate(resultDto);
  }

  public boolean isDone() {
    return query.size() == result.size();
  }
}

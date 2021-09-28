package pics.udhd.kafka;

import pics.udhd.kafka.dto.QueryResultDto;

public interface QueryCallback {

  void execute(QueryResultDto result);
  void fail(QueryResultDto result);
}

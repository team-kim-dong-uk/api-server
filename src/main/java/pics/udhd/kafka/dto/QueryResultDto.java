package pics.udhd.kafka.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import pics.udhd.kafka.dto.internal.ResultDto;

/**
 * QueryResultDto is only used by QueryCommander.
 * @see pics.udhd.kafka.QueryCommander
 */
public class QueryResultDto {
  @Getter
  private Map<String, List<String>> value;

  public QueryResultDto() {
    this.value = new HashMap<>();
  }

  public int size() {
    return value.size();
  }

  public void accumulate(ResultDto resultDto) throws Exception{
    if (resultDto.getPhotoId() != null) {
      value.put(resultDto.getPhotoId(), resultDto.getValue());
    } else if (resultDto.getUrl() != null) {
      value.put(resultDto.getUrl(), resultDto.getValue());
    } else {
      throw new Exception("unsupported resultDto type");
    }
  }
}

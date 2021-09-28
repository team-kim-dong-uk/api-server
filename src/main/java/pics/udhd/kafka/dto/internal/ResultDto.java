package pics.udhd.kafka.dto.internal;

import java.util.List;
import java.util.UUID;
import lombok.Data;

/**
 * [en]
 * ResultDto is a wrapper class for search query result on queue.
 * It is abstraction because don't know how the shape of the result will change.
 *
 * [ko]
 * Result 는 검색 결과을 위한 정보 전달 클래스
 * 결과의 형태가 어떻게 바뀔지 모르기 때문에 추상화
 *
 * @see pics.udhd.kafka.QueryExecutor
 * @see pics.udhd.kafka.QueryCommander
 * @see QueryDto
 */
@Data
public class ResultDto {
  UUID queryKey;
  String photoId;
  List<String> value; /* 겹치는 그림 ID */

  public ResultDto() {}
  public int size() {
    return value.size();
  }
}

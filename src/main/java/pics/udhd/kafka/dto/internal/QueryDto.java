package pics.udhd.kafka.dto.internal;

import java.util.UUID;
import lombok.Data;
import pics.udhd.kafka.dto.PhotoDto;

/**
 * [en]
 * QueryDto is a wrapper class for search query on queue.
 * It is abstraction because don't know how the shape of the query will change.
 *
 * [ko]
 * QueryDto 는 검색을 위한 정보 전달 클래스
 * query의 형태가 어떻게 바뀔지 모르기 때문에 추상화
 *
 * @see pics.udhd.kafka.QueryExecutor
 * @see pics.udhd.kafka.QueryComamnder
 * @see ResultDto
 */
@Data
public class QueryDto {
  public UUID key;
  public PhotoDto photo;
  public QueryDto() {}

  public QueryDto(UUID key, PhotoDto photoDto) {
    this.key = key;
    this.photo = photoDto;
  }

  public String getPhotoId() {
    return photo.getPhotoId();
  }
}

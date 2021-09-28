package pics.udhd.kafka.dto.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import pics.udhd.kafka.dto.PhotoDto;

/**
 * [en]
 * InsertDto is a wrapper class for insertion query on queue.
 * It is abstraction because don't know how the shape of the insertion query will change.
 *
 * [ko]
 * QueryDto 는 삽입을 위한 정보 전달 클래스
 * query의 형태가 어떻게 바뀔지 모르기 때문에 추상화
 *
 * @see pics.udhd.kafka.QueryExecutor
 * @see pics.udhd.kafka.QueryComamnder
 * @see ResultDto
 */
@Data
public class InsertDto {
  @JsonProperty("photoId")
  String photoId;
  @JsonProperty("url")
  String url;

  /**
   * It is needed by Jackson on kafka jsondeserializer
   */
  public InsertDto() {}
  public InsertDto(String photoId, String url) {
    this.photoId = photoId;
    this.url = url;
  }

  public InsertDto(PhotoDto photoDto) {
    this.photoId = photoDto.getPhotoId();
    this.url = photoDto.getUrl();
  }
}

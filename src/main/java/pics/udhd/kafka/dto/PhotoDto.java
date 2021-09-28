package pics.udhd.kafka.dto;

import lombok.Data;

/**
 * [en] PhotoDto is a wrapper class for data.
 * PhotoDto needs only photoId and url currently.
 *
 * [ko] PhotoDto 는 현재 photoId, url만 현재 필요함.
 */
@Data
public class PhotoDto {
  String photoId;
  String url;
}

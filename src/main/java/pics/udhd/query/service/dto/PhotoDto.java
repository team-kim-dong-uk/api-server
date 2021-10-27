package pics.udhd.query.service.dto;

import lombok.Builder;
import lombok.Data;

/**
 * [en] PhotoDto is a wrapper class for data.
 * PhotoDto needs only photoId and url currently.
 *
 * [ko] PhotoDto 는 현재 photoId, url만 현재 필요함.
 */
@Data
@Builder
public class PhotoDto {
  String photoId;
  String url;
}

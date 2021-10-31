package com.udhd.apiserver.web.dto.photo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PhotoOutlineDto {

  /**
   * 사진의 id
   */
  private String photoId;
  /**
   * 미리보기용 저화질/용량 이미지 링크
   */
  private String thumbnailLink;
}

package com.udhd.apiserver.web.dto.photo;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TagListDto {

  /**
   * 추천된 태그 목록
   */
  private List<String> tags;
}

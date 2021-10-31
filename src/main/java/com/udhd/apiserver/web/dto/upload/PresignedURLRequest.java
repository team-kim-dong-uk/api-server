package com.udhd.apiserver.web.dto.upload;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class PresignedURLRequest {

  /**
   * 업로드 할 사진들의 checksum
   */
  List<String> checksums;
}

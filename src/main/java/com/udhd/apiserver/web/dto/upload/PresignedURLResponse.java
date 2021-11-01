package com.udhd.apiserver.web.dto.upload;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PresignedURLResponse {

  public String pollingKey; /* For progress */
  public List<String> checksums; /* For Debug */
  public List<String> urls;
  public List<String> photoIds; /* Duplicated photo */
}

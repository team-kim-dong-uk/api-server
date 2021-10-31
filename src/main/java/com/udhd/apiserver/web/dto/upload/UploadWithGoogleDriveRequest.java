package com.udhd.apiserver.web.dto.upload;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UploadWithGoogleDriveRequest {

  private String googleDriveToken;
  private List<String> fileIds;
}

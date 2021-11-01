package com.udhd.apiserver.web.dto.upload;

import com.udhd.apiserver.web.dto.GeneralResponse;
import lombok.Data;

@Data
public class PresignedURLProgressResponse implements GeneralResponse {
  String photoId;
  Long progress;
}

package com.udhd.apiserver.web.dto.upload;


import java.util.List;
import lombok.Data;

@Data
public class TagUploadRequest {

  List<String> tags;
  Boolean propagate;
}

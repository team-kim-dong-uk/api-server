package com.udhd.apiserver.web.dto.feed;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class LikeDto {

  String id;
  String userId;
  String userName;
}

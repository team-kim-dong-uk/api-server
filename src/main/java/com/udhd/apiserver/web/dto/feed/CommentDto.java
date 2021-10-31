package com.udhd.apiserver.web.dto.feed;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentDto {

  String id;
  String userId;
  String userName;
  Long createdDate; // unix timestamp
  Long modifiedDate; // unix timestmap
  boolean deleted;
  String content;
}

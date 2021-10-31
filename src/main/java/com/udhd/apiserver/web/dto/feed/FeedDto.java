package com.udhd.apiserver.web.dto.feed;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FeedDto {

  String id;
  PhotoDto photo; // TODO: 그냥 domain 채로 가져오는데 숨겨야할 정보가 있으면 따로 인터페이스 만들어줘야함
  List<CommentDto> comments;
  List<LikeDto> likes;
  boolean liked;
  boolean saved;
}

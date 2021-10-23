package com.udhd.apiserver.web.dto.feed;

import com.udhd.apiserver.domain.feed.Comment;
import com.udhd.apiserver.domain.photo.Photo;
import java.util.List;
import lombok.Builder;

@Builder
public class FeedDto {
  String id;
  Photo photo; // TODO: 그냥 domain 채로 가져오는데 숨겨야할 정보가 있으면 따로 인터페이스 만들어줘야함
  List<Comment> comments;
}

package com.udhd.apiserver.domain.feed;

import com.udhd.apiserver.domain.photo.Photo;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

@Data
@Builder
public class Feed {
  @Id
  ObjectId id;

  private Photo photo;
  private List<Comment> comments;
}

package com.udhd.apiserver.domain.feed;

import com.udhd.apiserver.domain.photo.Photo;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "feed")
public class Feed implements Persistable<ObjectId> {

  @Id
  ObjectId id;

  private Photo photo;
  private List<Comment> comments;
  private List<Like> likes;

  @CreatedDate
  private LocalDateTime createdDate;

  @LastModifiedDate
  private LocalDateTime modifiedDate;

  private Long createdTimestamp;
  private Long order;

  @Override
  public boolean isNew() {
    return createdDate == null;
  }
}

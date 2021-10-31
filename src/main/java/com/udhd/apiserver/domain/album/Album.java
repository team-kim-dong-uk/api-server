package com.udhd.apiserver.domain.album;

import java.time.LocalDateTime;
import java.util.Date;
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

@NoArgsConstructor
@Builder
@AllArgsConstructor
@Document(collection = "album")
@Data
public class Album implements Persistable<ObjectId> {

  boolean deleted;
  @Id
  private ObjectId id;
  private ObjectId userId;
  private ObjectId feedId;
  private String thumbnailLink;
  private Date lastViewed;
  private List<String> tags;
  @CreatedDate
  private LocalDateTime createdDate;

  @LastModifiedDate
  private LocalDateTime modifiedDate;

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  @Override
  public boolean isNew() {
    return createdDate == null;
  }
}

package com.udhd.apiserver.domain.taggedphoto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * A tagged image class for database.
 */
@Document(collection = "TAGGED_IMAGE")
@Data
@Builder
public class TaggedPhotoVO {
  @Id
  private ObjectId photoId;
  private String hash;
  private String url;

  @CreatedDate
  private LocalDateTime createdDate;

  @LastModifiedDate
  private LocalDateTime modifiedDate;
}

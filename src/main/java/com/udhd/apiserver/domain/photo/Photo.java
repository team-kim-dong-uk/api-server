package com.udhd.apiserver.domain.photo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Document(collection = "photo")
public class Photo {

  @Id
  private ObjectId id;
  private ObjectId uploaderId;
  private String thumbnailLink;
  private String originalLink;
  private String checksum;
  private List<String> tags;

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  @CreatedDate
  private LocalDateTime createdDate;

  @LastModifiedDate
  private LocalDateTime modifiedDate;

  public static String DEFAULT_SORT = "id";
  public static String HEAD_ID = "000000000000000000000000";
}
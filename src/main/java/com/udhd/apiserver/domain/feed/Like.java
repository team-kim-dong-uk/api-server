package com.udhd.apiserver.domain.feed;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;

@Data
@Builder
@AllArgsConstructor
public class Like implements Persistable<ObjectId> {
  @Id
  ObjectId userId;
  String userName;

  @CreatedDate
  private LocalDateTime createdDate;

  @LastModifiedDate
  private LocalDateTime modifiedDate;

  @Override
  public ObjectId getId() {
    return userId;
  }

  @Override
  public boolean isNew() {
    return createdDate == null;
  }
}
